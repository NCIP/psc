module Buildr::Idea7x

  IPR_TEMPLATE_ABSOLUTE = File.join(
    Gem.path.map { |p| File.join(p, "gems/buildr-#{Buildr::VERSION}") }.detect { |f| File.exist? f },
    "lib/buildr/ide/idea7x.ipr.template"
  )

  def self.generate_ipr(project, idea7x, sources)
    task_name = project.path_to("#{project.name.gsub(':', '-')}-7x.ipr")
    idea7x.enhance [ file(task_name) ]
    file(task_name=>sources) do |task|
      info "Writing #{task.name}"

      # Generating just the little stanza that chanages from one project to another
      partial = StringIO.new
      xml = Builder::XmlMarkup.new(:target=>partial, :indent=>2)
      xml.component(:name=>"ProjectModuleManager") do
        xml.modules do
          project.projects.each do |subp|
            module_name = subp.name.gsub(":", "-")
            if subp.base_dir
              module_path = subp.base_dir.gsub(/^#{project.base_dir}\//, '')
            else
              module_path = subp.name.split(":"); module_path.shift
              module_path = module_path.join(File::SEPARATOR)
            end
            path = "#{module_path}/#{module_name}#{IML_SUFFIX}"
            xml.module :fileurl=>"#{PROJECT_DIR_URL}/#{path}", :filepath=>"#{PROJECT_DIR}/#{path}"
          end
          if package = project.packages.first
            xml.module :fileurl=>"#{PROJECT_DIR_URL}/#{project.name}#{IML_SUFFIX}", :filepath=>"#{PROJECT_DIR}/#{project.name}#{IML_SUFFIX}"
          end
        end
      end

      # Loading the whole fairly constant crap
      template_xml = REXML::Document.new(File.open(IPR_TEMPLATE_ABSOLUTE))
      include_xml = REXML::Document.new(partial.string)
      template_xml.root.add_element(include_xml.root)
      File.open task.name, 'w' do |file|
        template_xml.write file
      end
    end
  end

end