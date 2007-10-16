# Gives buildr the ability to download snapshots created by m2.  This sort
# of snapshot's name includes a timestamp and a build number instead of the
# "SNAPSHOT" string implied by its version string.  The timestamp and build 
# number may be derived from "maven-metadata.xml," available alongside. 
module Buildr::ActsAsArtifact
  def snapshot?
    version =~ /-SNAPSHOT$/
  end
  
  def group_path
    group.gsub(".", "/")
  end
end

class Buildr::Artifact

  protected
  
  # Bits of this are copied from Artifact#download in buildr-1.2.6
  def download_with_snapshot_resolution
    begin
      download_without_snapshot_resolution
    rescue RuntimeError => failure
      if snapshot? and failure.message =~ /^Failed to download/
        Buildr.repositories.remote.find do |repo_url|
          repo_url = URI.parse(repo_url) unless URI === repo_url
          repo_url.path += "/" unless repo_url.path[-1] == "/"
          
          snapshot_url = current_snapshot_repo_url repo_url
          if snapshot_url
            begin
              URI.download snapshot_url, name
              true
            rescue URI::NotFoundError
              false
            end
          else
            false
          end
        end or raise failure
      else
        # re-raise all other errors
        raise failure
      end
    end
  end
  alias_method_chain :download, :snapshot_resolution
  
  def current_snapshot_repo_url(repo_url)
    begin
      metadata_path = group_path + "/#{id}/#{version}/maven-metadata.xml"
      metadata_xml = StringIO.new
      URI.download repo_url + metadata_path, metadata_xml
      metadata = REXML::Document.new(metadata_xml.string).root
      timestamp = REXML::XPath.first(metadata, "//timestamp").text
      build_number = REXML::XPath.first(metadata, "//buildNumber").text
      repo_url + "#{group_path}/#{id}/#{version}/#{id}-#{version[0, version.size - 9]}-#{timestamp}-#{build_number}.#{type}"
    rescue URI::NotFoundError
      nil
    end
  end
end