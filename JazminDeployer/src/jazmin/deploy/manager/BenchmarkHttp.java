/**
 * 
 */
package jazmin.deploy.manager;

import java.util.Map;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

/**
 * @author yama
 *
 */
public class BenchmarkHttp {
	static interface SampleAction{
		Object run()throws Exception;
	}
	//
	BenchmarkSession session;
	private static final String DEFAULT_UA="JazminDeployer HttpRobot";
	AsyncHttpClientConfig.Builder clientConfigBuilder;
	AsyncHttpClientConfig clientConfig;
	AsyncHttpClient asyncHttpClient;
	//
	public BenchmarkHttp(BenchmarkSession session) {
		this.session=session;
		clientConfigBuilder=new Builder();
		clientConfigBuilder.setUserAgent(DEFAULT_UA);
		clientConfig=clientConfigBuilder.build();
		asyncHttpClient = new AsyncHttpClient(new NettyAsyncHttpProvider(clientConfig),clientConfig);
	}
	//
	private Object sample(String name,SampleAction action){
		long start=System.currentTimeMillis();
		boolean error=false;
		try{
			return action.run();
		}catch (Exception e) {
			session.log(e.getMessage());
			error=true;
			return null;
		}finally {
			session.sample(name,System.currentTimeMillis()-start, error);
		}
	}
	//
	public String post(String url){
		return post(url,null,null,null);
	}
	//
	public String post(String url,
			Map<String,String>headers,
			Map<String,String>parameters,
			Map<String,String> formValues){
		return (String) sample(url,()->{
			 BoundRequestBuilder builder=asyncHttpClient.preparePost(url);
			 if(headers!=null){
				 headers.forEach((k,v)->{
					 builder.addHeader(k, v);
				 });
			 }
			 if(parameters!=null){
				 parameters.forEach((k,v)->{
					 builder.addQueryParam(k, v);
				 });
			 }
			 if(formValues!=null){
				 formValues.forEach((k,v)->{
					 builder.addFormParam(k, v);
				 });
			 }
			 return builder.execute().get().getResponseBody();
		});
	}
	//
	public String get(String url,
			Map<String,String>headers,
			Map<String,String>parameters){
		return (String) sample(url,()->{
			 BoundRequestBuilder builder=asyncHttpClient.prepareGet(url);
			 if(headers!=null){
				 headers.forEach((k,v)->{
					 builder.addHeader(k, v);
				 });
			 }
			 if(parameters!=null){
				 parameters.forEach((k,v)->{
					 builder.addQueryParam(k, v);
				 });
			 }
			 return builder.execute().get().getResponseBody();
		});
	}
}