# Defines a DSL for building valid templates for testing
# Allows the user to specify the template to any level, then provides
# defaults below that level to ensure the template is complete

module TemplateBuilder
  def create_study(assigned_identifier)
    study = Psc::Service::TemplateSkeletonCreator::BLANK.create(assigned_identifier)
    study.development_amendment.deltas.clear # BLANK creator includes one epoch for compat with UI
    PscTest.log study.development_amendment.deltas
    application_context['studyService'].save(study)
    yield StudyBuilder.new(study)
    application_context['amendmentService'].amend(study)
    study
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
    
    def default_epoch(name)
      Psc::Domain::Epoch.new.tap do |e|
        e.name = name
        e.add_study_segment(default_study_segment(name))
      end
    end
    
    def default_study_segment(name)
      Psc::Domain::StudySegment.new.tap do |ss|
        ss.name = name
        ss.add_period(default_period)
      end
    end
    
    def default_period(name=nil)
      Psc::Domain::Period.new.tap do |p|
        p.name = name || 'default'
        p.duration.unit = Psc::Domain::Duration::Unit::day
        p.duration.quantity = 21
        p.repetitions = 1
        p.start_day = 1
        default_activities.each { |pa| p.add_planned_activity(pa) }
      end
    end
    
    def default_activities
      [
        create_planned_activity("Glucose",   2),
        create_planned_activity("Platelets", 3),
        create_planned_activity("Tylenol",   5),
        create_planned_activity("EKG",       8)
      ]
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

    def epoch(name)
      if block_given?
        Psc::Domain::Epoch.new.tap do |e|
          e.name = name
          add(e)
          yield EpochBuilder.new(e)
        end
      else
        add(default_epoch(name))
      end
    end
  end
  
  class EpochBuilder < Struct.new(:node)
    include BuilderBase

    def study_segment(name)
      if block_given?
        Psc::Domain::StudySegment.new.tap do |ss|
          ss.name = name
          add(ss)
          yield StudySegmentBuilder.new(ss)
        end
      else
        add(default_study_segment(name))
      end
    end
  end
  
  class StudySegmentBuilder < Struct.new(:node)
    include BuilderBase

    def period(name, options={})
      if block_given?
        Psc::Domain::Period.new.tap do |p|
          p.name = name
          duration_quantity, duration_unit = options[:duration]
          duration_unit = duration_unit ? :day : duration_unit.to_s.sub(/s$/, '').to_sym
          p.duration.unit = Psc::Domain::Duration::Unit.send(duration_unit)
          p.duration.quantity = duration_quantity
          p.repetitions = options[:repetitions] || 1
          p.start_day = options[:start_day] || 1
          add(p)
          yield PeriodBuilder.new(p)
        end
      else
        add(default_period(name))
      end
    end
  end
  
  class PeriodBuilder < Struct.new(:node)
    include BuilderBase
    
    def activity(name, day)
      add(create_planned_activity(name, day))
    end
  end
end

class Spec::Example::ExampleGroup
  include TemplateBuilder
end