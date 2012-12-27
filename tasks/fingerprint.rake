#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

require 'yaml'
require 'digest/sha1'

module Buildr::PSC
  module Fingerprint
    def self.write_fingerprints(target_path)
      mkdir_p(File.dirname(target_path))
      File.open(target_path, 'w') do |f|
        f.write fingerprints.to_yaml
      end
    end

    def self.check_fingerprints(other_fp_file)
      other_fp = YAML.load(File.read(other_fp_file))
      these_fp = fingerprints
      fp_mismatches = []
      missing_here = []
      missing_there = []

      # this works because the entries are sorted by spec
      t = o = 0
      until t >= these_fp.size && o >= other_fp.size
        this  = these_fp[t]
        other = other_fp[o]
        more_t = t < these_fp.size
        more_o = o < other_fp.size

        trace "Comparing #{this.inspect} and #{other.inspect}"

        if !more_o || (more_t && this[:spec] < other[:spec])
          trace "  ! missing from other env"
          missing_there << this[:spec]
          t += 1
        elsif !more_t || (more_o && this[:spec] > other[:spec])
          trace "  ! missing from current env"
          missing_here << other[:spec]
          o += 1
        else
          unless this[:fingerprint] == other[:fingerprint]
            trace "  ! fingerprints don't match"
            fp_mismatches << this[:spec]
          else
            trace "  - matched"
          end

          t += 1
          o += 1
        end
      end

      if fp_mismatches.empty? && missing_here.empty? && missing_there.empty?
        info "Fingerprints match"
      else
        msg = "The fingerprint doesn't match the build current environment.\n"
        unless fp_mismatches.empty?
          msg << "#{fp_mismatches.size} artifact fingerprint #{plural(fp_mismatches.size, 'mismatch', 'es')}:\n" <<
            " - " << fp_mismatches.join("\n - ") << "\n"
        end
        unless missing_here.empty?
          msg << "#{missing_here.size} #{plural(missing_here.size, 'artifact')} in the fingerprint #{form_of_be(missing_here.size)} not used in this build:\n" <<
            " - " << missing_here.join("\n - ") << "\n"
        end
        unless missing_there.empty?
          msg << "#{missing_there.size} #{plural(missing_there.size, 'artifact')} used in this build #{form_of_be(missing_there.size)} not in the fingerprint:\n" <<
            " - " << missing_there.join("\n - ") << "\n"
        end

        fail msg
      end
    end

    private

    def self.form_of_be(count)
      count == 1 ? "is" : "are"
    end

    def self.plural(count, base, plural_ending = 's')
      count == 1 ? base : base + plural_ending
    end

    def self.fingerprints
      Artifact.list.inject([]) do |fps, artifact_spec|
        artifact = Artifact.lookup(artifact_spec)
        unless internal_artifact?(artifact)
          fps << {
            :spec => artifact_spec,
            :fingerprint => fingerprint(artifact)
          }
        end
        fps
      end.sort_by { |a| a[:spec] }
    end

    def self.fingerprint(artifact)
      Digest::SHA1.hexdigest(File.read(artifact.name))
    end

    def self.internal_artifact?(artifact)
      basedir = File.expand_path('../..', __FILE__)
      artifact.name =~ /^#{basedir}/
    end
  end
end

desc "Generate a fingerprint of the artifacts used in this build"
task :fingerprint => :artifacts do
  Buildr::PSC::Fingerprint.write_fingerprints(
    File.expand_path('../../reports/artifact_fingerprints.yml', __FILE__))
end

desc "Check that this build is using the same artifacts as another one"
task :check_fingerprint => :artifacts do
  other_fp = ENV['FINGERPRINT'] or
    raise "Please specify the fingerprint file to check against using FINGERPRINT=filename"
  Buildr::PSC::Fingerprint.check_fingerprints(other_fp)
end
