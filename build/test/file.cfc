component {

	public function init(string apikey) {
		variables.apikey = arguments.apikey;
		return this;
	}
	
	public struct function parse(string text="", string sourceurl="", string analysis = "all") {
		var theURL = "http://portaltnx20.openamplify.com/AmplifyWeb_v20/AmplifyThis";
		var http = new com.adobe.coldfusion.http();

		http.setMethod("post");
		http.addParam(name="apiKey", value="#variables.apiKey#",type="formfield");		
	
		if(len(arguments.text)) http.addParam(name="inputText",value=arguments.text,type="formfield");
		else if(len(arguments.sourceurl)) http.addParam(name="sourceURL",value=arguments.sourceURL,type="formfield");
		else {
			throw(message="Must specify text or sourceurl.");
		}
		
		http.addParam(name="analysis", value=arguments.analysis, type="formfield");
		http.addParam(name="outputFormat", value="json", type="formfield");
		
		http.setURL(theUrl);
		var res = http.send().getPrefix().fileContent;

		if(isJSON(res)) {
			var data = deserializeJSON(res);
			return data["ns1:AmplifyResponse"].AmplifyReturn;
		} else throw(message="Invalid response");
	}
	
