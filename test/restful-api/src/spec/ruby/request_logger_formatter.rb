require 'spec/runner/formatter/base_text_formatter'
require 'haml'
require 'socket'
require 'webrick/httprequest'
require 'stringio'

# Records the all the HTTP request/response pairs associated with each
class RequestLoggerFormatter < Spec::Runner::Formatter::BaseTextFormatter
  attr_reader :proxy_server

  def initialize(options, output)
    super
    @proxy_server = start_proxy
    @current_group = nil
    @current_record = nil

    @@instance = self
  end

  def self.proxy_url
    if @@instance && @@instance.proxy_server
      "http://localhost:#{@@instance.proxy_server.port}/"
    end
  end

  def start(example_count)
    output.puts File.read(__FILE__.sub(/.rb/, '_head.html'))
  end

  def example_group_started(new_group)
    if !@current_group || @current_group.nested_descriptions.first != new_group.nested_descriptions.first
      # new top-level group starting
      output.puts "</div>" unless @current_group.nil?
      output.puts "<div class='group'>"
    end
    @current_group = new_group
  end

  def example_started(new_example)
    @current_record = ExampleRecord.new(@current_group, new_example)
  end

  def example_passed(example)
    output.puts @current_record.to_html
    @current_record = nil
  end

  def example_failed(example, counter, failure)
    output.puts @current_record.to_html if ENV['REQUEST_LOG_INCLUDE_ALL']
    @current_record = nil
  end

  def example_pending(example, message)
    output.puts @current_record.to_html if ENV['REQUEST_LOG_INCLUDE_ALL']
    @current_record = nil
  end

  def start_dump
    output.puts "</body>\n</html>"
  end

  def dump_summary(duration, example_count, failure_count, pending_count)
    # noop override
  end

  def dump_failure(counter, failure)
    # noop override
  end

  def dump_pending
    # noop override
  end

  def close
    super
    @@instance = nil
  end

  def record_http_cycle(req, resp)
    @current_record.cycles << [req, resp]
  end

  private

  def start_proxy
    formatter = self
    server = TransparentHttpProxy.new(
      ENV['JETTY_PORT'] ? ENV['JETTY_PORT'].to_i + 1 : 7201,
      Proc.new do |req, res|
        formatter.record_http_cycle(req, res)
      end
    )
    Thread.new { server.start }
    server
  end

  class ExampleRecord < Struct.new(:parent_group, :example_proxy)
    def cycles
      @cycles ||= []
    end

    def source
      file, line = example_proxy.location.split('/').last.split(':')
      "From #{file}, line #{line}"
    end

    def description
      parent_group.nested_descriptions.join(' ') + ' ' + example_proxy.description
    end

    def to_html
      Haml::Engine.new(<<-HAML).render(self)
.example
  .h2= description
  .source= source
  - cycles.each_with_index do |cycle, i|
    .cycle{ :class => ('first' if i == 0) }
      .http.request
        %h3 Request
        %pre.content&= cycle.first
      .http.response
        %h3 Response
        %pre.content&= cycle.last
      HAML
    end
  end

  # Super-primitive transparent, recording HTTP Proxy
  class TransparentHttpProxy
    attr_reader :port

    def initialize(port, recorder)
      @port = port
      @recorder = recorder
    end

    def start
      proxy_socket = TCPServer.open('0.0.0.0', @port)
      $stderr.puts "#{self.class.name} started on #{@port}"
      loop {
        cycle = ProxyCycle.new(proxy_socket.accept).start
        @recorder.call(cycle.request_text, cycle.response_text)
      }
    end

    class ProxyCycle
      attr_accessor :request_text, :response_text

      def initialize(client)
        @client = client
      end

      def start
        read_request
        propagate_request
        propagate_response
        self
      end

      def request
        @request ||= begin
          r = WEBrick::HTTPRequest.new({})
          r.parse(StringIO.new(request_text))
          r
        end
      end

      private

      def debug(msg=nil)
        if ENV['VERBOSE']
          if block_given?
            $stderr.puts yield
          else
            $stderr.puts msg
          end
        end
      end

      def log_lines(heading, http_message)
        heading + http_message.gsub(/\r\n/, "\r\n#{heading}")
      end

      def read_request
        debug "read_request"
        @request_text = ""
        until @request_text =~ /\r\n\r\n/ || (line = @client.gets).nil?
          @request_text += line
        end
        read_entity_if_necessary
        debug { log_lines("read_request: ", request_text) }
      end

      def read_entity_if_necessary
        if @request_text =~ /Transfer-Encoding:/
          msg = "Lucky you: you get to implement Transfer-Encoding in #{__FILE__}!"
          $stderr.puts '*' * msg.length
          $stderr.puts msg
          $stderr.puts "This request will fail."
          $stderr.puts '*' * msg.length
          # If you are the lucky winner, see section 4.4 of RFC 2616.
        elsif @request_text =~ /Content-Length:\s+(\d+)/
          content_length = $1.to_i
          debug { "Attempting to read #{content_length} bytes" }
          @request_text += @client.read(content_length)
        end
      end

      def propagate_request
        debug { "propagate_request to #{request.host}:#{request.port}" }
        http = TCPSocket.open(request.host, request.port)
        debug { log_lines("corrected_request_text: ", corrected_request_text) }
        http << corrected_request_text
        @response_text = ""
        while line = http.gets
          debug { "recvd: #{line}" }
          @response_text += line
        end
        http.close
      end

      def propagate_response
        debug "propagate_response"
        debug { log_lines("response_text: ", response_text) }
        @client << response_text
        @client.close
      end

      # Remove & replace Connection headers, if any
      def corrected_request_text
        request_text.
          gsub(/^Proxy-Connection:.*\r\n/, ''). # don't care about current value -- always close
          gsub(/^Connection:.*\r\n/, ''). # don't care about current value -- always close
          sub(/\r\n\r\n/, "\r\nConnection: close\r\n\r\n") # add Connection: close as last header
      end
    end
  end
end

