import customFetch from "./custom-fetch";

export default class ConfigService {

  fetchSiteMapFooter = async () => {
    return await customFetch('/api/config/siteMapFooter');
  };
}
