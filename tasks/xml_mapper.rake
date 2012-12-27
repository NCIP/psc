#L
# Copyright Northwestern University.
#
# Distributed under the OSI-approved BSD 3-Clause License.
# See http://ncip.github.com/psc/LICENSE.txt for details.
#L

##Custom mapper to allow xml insertion via filters

require 'buildr/core/filter'
require "rexml/document"
include REXML
module Buildr
  class Filter
    class Mapper
      def xml_transform(content, path = nil)
        puts "content xml= #{content}"
        doc = Document.new content
        xpath = yield :xpath
        puts "xpath= #{xpath}"
        xml_content = yield :xml_content
        type = (yield :insert_type).to_sym
        xml_content = "<psc_test_root>"+xml_content+"</psc_test_root>"
        parent_xml_element = XPath.first( doc,  xpath)
        puts "parent_xml_element= #{parent_xml_element}"
        Document.new(xml_content).root.elements.each { |xml_element_to_insert| 
          puts "xml_element_to_insert= #{xml_element_to_insert}"
          if(type == :before)
            doc.root.insert_before parent_xml_element, xml_element_to_insert
          elsif(type == :after)
            doc.root.insert_after parent_xml_element, xml_element_to_insert
          elsif(type == :under)
            parent_xml_element.add_element(xml_element_to_insert)
          end 
        }
        output = "" 
        doc.write output
        output
      end
    end    
  end
end