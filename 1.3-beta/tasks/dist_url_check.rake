# Adds an m2-ish message for non-downloadable artifacts which provide a pointer
# to where they can be downloaded

module Buildr::ActsAsArtifact
  def repo_path
    group.gsub(".", "/") + "/#{id}/#{version}/#{File.basename(name)}"
  end
end

class Buildr::Artifact
  protected
  
  def download_with_dist_url_check()
    begin
      download_without_dist_url_check
    rescue RuntimeError => failure
      if failure.message =~ /^Failed to download/ and type != :pom
        u = download_distribution_url
        if u
          fail "#{to_spec} is not available for repository download.\nYou can get it manually:\n- Download it from\n    #{u}\n- Copy it into your local repository as\n    #{name}"
        else
          # re-raise original failure if no distribution url found
          raise failure
        end
      else
        # re-raise all other errors
        raise failure
      end
    end
  end
  alias_method_chain :download, :dist_url_check
  
  # Looks for the remote POM for this artifact and extracts the
  # project/distributionManagement/downloadUrl value if it exists.
  # Bits of this are copied from Artifact#download in buildr-1.2.6
  def download_distribution_url()
    pom = pom()
    pom_path = pom.repo_path
    Buildr.repositories.remote.each do |repo_url|
      repo_url = URI.parse(repo_url) unless URI === repo_url
      repo_url.path += "/" unless repo_url.path[-1] == "/"
      begin
        pom_xml = StringIO.new
        URI.download repo_url + pom_path, pom_xml
        content = POM.new(pom_xml.string)
        dm = content.project['distributionManagement']
        return dm[0]['downloadUrl'][0] if dm and dm[0] and dm[0]['downloadUrl']
      rescue URI::NotFoundError
        # move along
      end
    end
    nil
  end
end
