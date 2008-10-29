def application_context
  @application_context ||= Class.new do
    def initialize
      File.cd(File.expand_path("../..", File.dirname(__FILE__))) do
        @context = Java::OrgSpringframeworkContextSupport::GenericApplicationContext.new(
          Java::OrgSpringframeworkBeansFactoryXml::XmlBeanFactory.new(
            Java::OrgSpringframeworkCoreIo::FileSystemResource.new(
              File.expand_path("static-data/applicationContext.xml", File.dirname(__FILE__))
            ),
            Java::EduNorthwesternBioinformaticsStudycalendarTest::StudyCalendarTestHelper.getDeployedApplicationContext()
          )
        )
      end
    end

    def [](beanName)
      @context.getBean(beanName.to_s)
    end

    def method_missing(m, *args)
      @context.send(m, *args)
    end
  end.new
end
