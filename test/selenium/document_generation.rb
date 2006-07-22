require 'test/unit/ui/testrunnermediator'
require 'test/unit/ui/testrunnerutilities'

module Test
  module Unit
    module UI
      module DocumentGeneration

        # Runs a Test::Unit::TestSuite on the console.
        class TestRunner
          extend TestRunnerUtilities
          
          @@documenter = nil
          
          def self.documenter
            @@documenter
          end
          
          # Creates a new TestRunner for running the passed
          # suite. If quiet_mode is true, the output while
          # running is limited to progress dots, errors and
          # failures, and the final result. io specifies
          # where runner output should go to; defaults to
          # STDOUT.
          def initialize(suite, output_level=NORMAL, io=STDOUT)
            if (suite.respond_to?(:suite))
              @suite = suite.suite
            else
              @suite = suite
            end
            if (suite.respond_to?(:documenter=))
              suite.documenter = self
            else
              puts "Suite does not respond to documenter=, #{suite}, #{suite.class}"
            end
            @output_level = output_level
            @io = io
            @already_outputted = false
            @faults = []
            @text = ""
            @@documenter = self
          end

          # Begins the test run.
          def start
            setup_mediator
            attach_to_mediator
            return start_mediator
          end

          private
          def setup_mediator
            @mediator = create_mediator(@suite)
            suite_name = @suite.to_s
            if ( @suite.kind_of?(Module) )
              suite_name = @suite.name
            end
            output("Loaded suite #{suite_name}")
          end
          
          def create_mediator(suite)
            return TestRunnerMediator.new(suite)
          end
          
          def attach_to_mediator
            @mediator.add_listener(TestResult::FAULT, &method(:add_fault))
            @mediator.add_listener(TestRunnerMediator::STARTED, &method(:started))
            @mediator.add_listener(TestRunnerMediator::FINISHED, &method(:finished))
            @mediator.add_listener(TestCase::STARTED, &method(:test_started))
            @mediator.add_listener(TestCase::FINISHED, &method(:test_finished))
          end
          
          def start_mediator
            return @mediator.run_suite
          end
          
          def add_fault(fault)
            @faults << fault
            output_single(fault.single_character_display, PROGRESS_ONLY)
            @already_outputted = true
          end
          
          def started(result)
            @suite_documents ||= {}
            @result = result
            output("Started")
          end
          
          def finished(elapsed_time)
            nl
            output("Finished in #{elapsed_time} seconds.")
            @faults.each_with_index do |fault, index|
              nl
              output("%3d) %s" % [index + 1, fault.long_display])
            end
            nl
            output(@result)
            document_suites
          end
          
          def test_started(name)
            output_single(name + ": ", VERBOSE)
            document_test(name)
          end
          
          def test_finished(name)
            output_single(".", PROGRESS_ONLY) unless (@already_outputted)
            output_single(name + ": ", VERBOSE)
            nl(VERBOSE)
            @already_outputted = false
            match = TEST_NAME_REGEX.match(name)
            if match
              suite_name = match[2]
              test_name = match[1]
              @suite_documents[suite_name] << @current_test_document
              @current_test_document = nil
            end
          end
          
          def nl(level=NORMAL)
            output("", level)
          end
          
          def output(something, level=NORMAL)
            @io.puts(something) if (output?(level))
            @io.flush
          end
          
          def output_single(something, level=NORMAL)
            @io.write(something) if (output?(level))
            @io.flush
          end
          
          def output?(level)
            level <= @output_level
          end
          
          public
          def document_step(step)
            document("  1. " + step + "\n")
          end
          
          def document_comment(comment)
            document("     " + comment + "\n")
          end
          
          protected
          def document(message)
            @current_test_document ||= ""
            @current_test_document << message
          end
          
          def document_suites
            @suite_documents.each_key do |suite_name|
              suite_title = suite_name[0..-5].gsub(/[(A-Z)]/) { |matched| " #{matched}" }.lstrip
              output_document "#{suite_title}"
              output_document "#{"=" * suite_title.length}"
              output_document
              output_document @suite_documents[suite_name].join("\n\n")
              
            end
          end
          
          def output_document(message = "")
            puts message
          end
          
          TEST_NAME_REGEX = /([^\(\)]+)\(([^\(\)]+)\)/
          def document_test(name)
            match = TEST_NAME_REGEX.match(name)
            if match
              suite_name = match[2]
              test_name = match[1]
              @suite_documents[suite_name] ||= []
              test_title = test_name_to_title(test_name)
              document "#{test_title}\n#{"-" * test_title.length}\n\n"
            end
          end

          def test_name_to_title(test_name)
            test_name[5..-1].gsub(/_/, ' ').capitalize
          end
        end
      end
    end
  end
end
