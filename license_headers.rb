#!/usr/bin/env ruby

######
# Run this script to add/update NCIP's mandatory license headers.
#
# Usage:
#   $ cd {PSC_WORKING_ROOT}
#   $ ruby license_headers.rb

class LicenseUpdater
  attr_reader :filename, :contents

  LICENSE_TOKEN = /\{LICENSE\}/

  LICENSE_HEADER_TEXT = <<-TEXT
Copyright Northwestern University.

Distributed under the OSI-approved BSD 3-Clause License.
See http://ncip.github.com/psc/LICENSE for details.
TEXT

  def initialize(filename)
    @filename = filename
    @contents = File.read(filename)
  end

  def update
    $stderr.print "Processing #{filename}..."

    new_contents =
      if contents =~ license_pattern
        $stderr.puts "updating existing comment."
        contents.sub(license_pattern, license_comment)
      else
        $stderr.puts "adding new comment."
        insert_new_license
      end
    File.open(filename, 'w') { |f| f.write(new_contents) }
  end

  def insert_new_license
    insert_point = license_insert_points.map { |re| contents.match(re) }.compact.first
    fail "No insert point matches for #{filename} from #{license_insert_points.inspect}" unless insert_point

    i = insert_point.end(0)

    contents.insert(i, "#{"\n" if i != 0}#{license_comment}#{"\n" if contents.slice(i, 1) != "\n"}")
  end

  def license_pattern
    @license_pattern ||= begin
      re_string = license_header_lines.collect { |line|
        if line =~ LICENSE_TOKEN
          line_prefix = line.sub(/\s*#{LICENSE_TOKEN.source}\s*/, '')
          "(#{line_prefix}.*?\n)+?"
        else
          Regexp.escape(line) + "\n"
        end
      }.join('')

      Regexp.new(re_string, Regexp::MULTILINE)
    end
  end

  def license_comment
    license_header_lines.collect { |line|
      if line =~ LICENSE_TOKEN
        LICENSE_HEADER_TEXT.split("\n").collect { |l|
          line.sub(LICENSE_TOKEN, l).gsub(/\s*$/, '')
        }.join("\n") + "\n"
      else
        "#{line}\n"
      end
    }.join('')
  end
end

class JavaLicenseUpdater < LicenseUpdater
  def license_insert_points
    [ /\A/ ]
  end

  def license_header_lines
    [
      "/*L",
      " * {LICENSE}",
      " */"
    ]
  end
end

class RubyLicenseUpdater < LicenseUpdater
  def license_insert_points
    [
      %r{#!.*?\n},
      /\A/
    ]
  end

  def license_header_lines
    [
      "#L",
      "# {LICENSE}",
      "#L"
    ]
  end
end

class XmlLicenseUpdater < LicenseUpdater
  def license_insert_points
    [
      %r{<\?xml.*?\?>\s*?\n},
      /\A/
    ]
  end

  def license_header_lines
    [
      "<!--L",
      "  {LICENSE}",
      "L--!>"
    ]
  end
end

{
  "**/*.java" => JavaLicenseUpdater,
  "**/*.rake" => RubyLicenseUpdater,
  "**/*.rb" => RubyLicenseUpdater,
  "buildfile" => RubyLicenseUpdater,
  "**/src/**/*.xml" => XmlLicenseUpdater
}.each do |pattern, updater|
  Dir[pattern].each do |filename|
    updater.new(filename).update
  end
end
