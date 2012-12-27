#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

# Defines a DSL for building valid templates for testing
# Allows the user to specify the template to any level, then provides
# defaults below that level to ensure the template is complete

module TemplateBuilder
  def create_study(assigned_identifier)
    study = Psc::Service::TemplateSkeletonCreator::BLANK.create(assigned_identifier)
    study.development_amendment.deltas.clear # BLANK creator includes one epoch for compat with UI
    application_context['studyService'].save(study)
    yield StudyBuilder.new(study)
    application_context['amendmentService'].amend(study)
    study
  end

  def update_study(study, mode)
    unless study.development_amendment
      last_amendment_date = study.amendment ? study.amendment.date : Java::JavaUtil::Date.new
      study.development_amendment = Psc::Domain::Delta::Amendment.new.tap do |a|
        a.date = Java::JavaUtil::Date.new(last_amendment_date.time + 2 * 86400000)
      end
      application_context['studyService'].save(study)
    end
    yield StudyUpdater.new(study)
  end

  module BuilderBase
    def update_dev_amendment(node, change)
      application_context['amendmentService'].updateDevelopmentAmendmentAndSave(
        node, [change].to_java(Psc::Domain::Delta::Change))
    end

    def add(new_child)
      update_dev_amendment(node, Psc::Domain::Delta::Add.create(new_child))
      new_child
    end

    def create_planned_activity(name, day)
      act = application_context["activityDao"].get_by_name(name)
      unless act
        act = Psc::Domain::Activity.new.tap do |a|
          a.name = name
          a.code = name
          a.type = application_context["activityTypeDao"].get_by_name("Other") or raise "Could not find default type"
        end
        application_context["activityDao"].save(act)
      end

      Psc::Domain::PlannedActivity.new.tap { |pa|
        pa.activity = act
        pa.day = day
      }
    end
  end

  class StudyBuilder < Struct.new(:study)
    include BuilderBase

    def planned_calendar
      yield PlannedCalendarBuilder.new(study)
    end

    def population(abbrev, name)
      add = Psc::Domain::Delta::Add.create(Psc::Domain::Population.new.tap { |p|
        p.abbreviation = abbrev
        p.name = name
      })
      application_context['amendmentService'].updateDevelopmentAmendmentForStudyAndSave(
        study, [add].to_java(Psc::Domain::Delta::Change))
    end
  end

  class PlannedCalendarBuilder < Struct.new(:study)
    include BuilderBase

    def node
      study.planned_calendar
    end

    def epoch(name, &epoch_def)
      Psc::Domain::Epoch.new.tap do |e|
        e.name = name
        add(e)
        (epoch_def || default_epoch_def(name)).call EpochBuilder.new(e)
      end
    end

    private

    def default_epoch_def(name)
      proc do |e|
        e.study_segment name
      end
    end
  end

  class EpochBuilder < Struct.new(:node)
    include BuilderBase

    def study_segment(name, &segment_def)
      Psc::Domain::StudySegment.new.tap do |ss|
        ss.name = name
        add(ss)
        (segment_def || default_segment_def).call(StudySegmentBuilder.new(ss))
      end
    end

    private

    def default_segment_def
      proc do |ss|
        ss.period 'default'
      end
    end
  end

  class StudySegmentBuilder < Struct.new(:node)
    include BuilderBase

    def period(name, options={}, &period_def)
      Psc::Domain::Period.new.tap do |p|
        p.name = name
        duration_quantity, duration_unit = options[:duration]
        duration_unit = duration_unit ? duration_unit.to_s.sub(/s$/, '').to_sym : :day
        p.duration.unit = Psc::Domain::Duration::Unit.send(duration_unit)
        p.duration.quantity = duration_quantity || 21
        p.repetitions = options[:repetitions] || 1
        p.start_day = options[:start_day] || 1
        add(p)
        (period_def || default_period_def).call PeriodBuilder.new(p)
      end
    end

    private

    def default_period_def
      proc do |p|
        p.activity("Glucose",   2)
        p.activity("Platelets", 3)
        p.activity("Tylenol",   5)
        p.activity("EKG",       8)
      end
    end
  end

  class PeriodBuilder < Struct.new(:node)
    include BuilderBase

    def activity(name, day)
      add(create_planned_activity(name, day))
    end
  end

  class StudyUpdater < Struct.new(:study)
    include BuilderBase

    def add_epoch(name, &epoch_def)
      PlannedCalendarBuilder.new(study).epoch(name, &epoch_def)
    end

    def add_study_segment(name, options=nil, &segment_def)
      target = (options || {}).delete :in
      raise "Please specify the name of the target epoch.  E.g. :in => 'Treatment'." unless target
      EpochBuilder.new( find_required_epoch(target) ).study_segment(name, &segment_def)
    end

    def add_period(name, options=nil, &period_def)
      options ||= {}
      target = options.delete :in
      raise "Please specify the name of the target study segment.  E.g. :in => 'Treatment: B'." unless target
      StudySegmentBuilder.new( find_required_segment(target) ).period(name, options, &period_def)
    end

    def add_planned_activity(name, day, options={})
      target = (options || {}).delete :in
      raise "Please specify the name of the target period.  E.g. :in => 'Treatment: B: P4'." unless target
      PeriodBuilder.new( find_required_period(target) ).activity(name, day)
    end

    private

    def find_required_epoch(name)
      study.planned_calendar.epochs.find { |e| e.name == name }.tap do |epoch|
        unless epoch
          raise "No epoch named #{name.inspect}.  Only #{study.planned_calendar.epochs.collect { |e| e.name }.inspect}"
        end
      end
    end

    def find_required_segment(name)
      epoch_name, segment_name = name.split(/\s*\:\s*/)
      epoch = find_required_epoch(epoch_name)
      epoch.study_segments.find { |ss| ss.name == segment_name }.tap do |ss|
        unless ss
          raise "No study segment named #{name.inspect}.  Only #{epoch.study_segments.collect { |ss| "#{epoch.name}: #{ss.name}" }.inspect}"
        end
      end
    end

    def find_required_period(name)
      epoch_name, segment_name, period_name = name.split(/\s*:\s*/)
      segment = find_required_segment("#{epoch_name}: #{segment_name}")
      segment.periods.find { |p| p.name == period_name }.tap do |p|
        unless p
          raise "No period named #{name.inspect}.  Only #{segment.periods.collect { |p| "#{segment.epoch.name}: #{segment.name}: #{p.name}" }.inspect}"
        end
      end
    end
  end
end

class Spec::Example::ExampleGroup
  include TemplateBuilder
end
