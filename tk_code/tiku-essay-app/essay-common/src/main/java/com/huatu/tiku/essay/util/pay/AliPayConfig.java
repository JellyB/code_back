package com.huatu.tiku.essay.util.pay;

/* *
 *类名：AliPayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */
                                                                                                                                                                                                                                                                                                                                                                                                                                                
public class AliPayConfig {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "2088411127750624";
	// 商户的私钥
	public static String private_key ="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBANV6eCVkHqPo4YkOHoHA0KCCfAtCs0VtWlwA7G4gX6WfsEuya4+3dQh2f8kn3vEmsJKMYhjQl6RqjDfCdwuJ66Mpnr0VdWDMQizn9KaLU7HtG5BfDzBxRoujNN3nfTyxdVPHCjS8VmFhZDL+y7NJInDt1Lgsq20DruY0GJJ1eG3VAgMBAAECgYEAxhIuvcqqXzypXif6iDtllzfabfxCJ42xxCGbPQbOl/aVdXajNCJs9SA1qmdTBH74X9DfB9UqjgPJ+8Zz/AHI3dXW6wcvtzRiVClcFXipVizgc0mEqNuOcrROZ4znc5y4zlxOBXt4xcJVT5UoMFPzetn3OWXNvkDJjvzIAB+wvjUCQQDtliIHGJOLM45reJI2I4TZbcv5QmwFgXYj7Flwe0qL1OWO/SdZ+bHVVBtkoGSxYHSyh3isMGHgyP4Vgi3xT3nfAkEA5gYEXq7JVp92mPGLZNERVgSaUYQgKTy+sP63TMFv/RUMYIqjgyHHIZ69y705mdewWik5tFxCaohgnARtO3p2ywJBAOd+EUm4uIo5gdtVb6EwmpEAWm5UOcxjiCkYcU0X1FrK5aGdKGqS0KN7f/VcEsCBqzMIrJuZyMStEmUCoqEtPyECQQDIl/a3mzV5lRaXyg0Fnky/9sOc0tw5GgAx2e9/wDEpQ3HHvx9Y+9vsNcLOKfZRcwcXmVv5LXu967BUXofjlqiBAkAAnUULjr19HM4UaAms9H9NkxIT7gRRMX+KUQdYc0IlO5gre2ZebC2CFrExKccmQfrycLz1/B8Gn9dcKwX8Y2fP\n";
	//             	"MIICXgIBAAKBgQDVenglZB6j6OGJDh6BwNCggnwLQrNFbVpcAOxuIF+ln7BLsmuPt3UIdn/JJ97xJrCSjGIY0Jekaow3wncLieujKZ69FXVgzEIs5/Smi1Ox7RuQXw8wcUaLozTd5308sXVTxwo0vFZhYWQy/suzSSJw7dS4LKttA67mNBiSdXht1QIDAQABAoGBAMYSLr3Kql88qV4n+og7ZZc32m38QieNscQhmz0Gzpf2lXV2ozQibPUgNapnUwR++F/Q3wfVKo4DyfvGc/wByN3V1usHL7c0YlQpXBV4qVYs4HNJhKjbjnK0TmeM53OcuM5cTgV7eMXCVU+VKDBT83rZ9zllzb5AyY78yAAfsL41AkEA7ZYiBxiTizOOa3iSNiOE2W3L+UJsBYF2I+xZcHtKi9Tljv0nWfmx1VQbZKBksWB0sod4rDBh4Mj+FYIt8U953wJBAOYGBF6uyVafdpjxi2TREVYEmlGEICk8vrD+t0zBb/0VDGCKo4MhxyGevcu9OZnXsFopObRcQmqIYJwEbTt6dssCQQDnfhFJuLiKOYHbVW+hMJqRAFpuVDnMY4gpGHFNF9RayuWhnShqktCje3/1XBLAgaszCKybmcjErRJlAqKhLT8hAkEAyJf2t5s1eZUWl8oNBZ5Mv/bDnNLcORoAMdnvf8AxKUNxx78fWPvb7DXCzin2UXMHF5lb+S17veuwVF6H45aogQJAAJ1FC469fRzOFGgJrPR/TZMSE+4EUTF/ilEHWHNCJTuYK3tmXmwtghaxMSnHJkH68nC89fwfBp/XXCsF/GNnzw==";
	// 支付宝的公钥，无需修改该值
	public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
	// app ID
	public static String app_id = "2016092601976957";
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "./";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "utf-8";
	
	// 签名方式 不需修改
	public static String sign_type = "RSA";
	
	//教育私钥
	public static String edu_private_key ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBANJQ5dZJ3uSix1ifbAvWKmoXi7kb2DMj82hEMaHFGk6dbMFw708WgqzjOvOGQPQV9Nvq423aSlCT1qO/Ow2dDbRLMHyi6e7L3e7Ab5dDL4osWxxqTd4z9ShkPmtwGCgIDSOdKF0zsdtEtrHKUZ0hmR4g7/D2hO0bYxItBCbg+kv3AgMBAAECgYAhHWnC/uigmVzIAHoTtwoAoGp4oAC/tKZrwWkLLqzAuhrYrn6Ptlym+jRbCcWKaTaftfFPZ7KR02VVbRPQRe1VL7JFcEga6Po6KZKbfNOf75T2mT0RZpwZpYjxa74yo47mnN4g+ZpyEhm1OsAGUNPQw/LloMGw2YANd9V1qdgd0QJBAP193dwfF7sI09upzsLljhCOcF4rZvJUgbnBANyKljWw7SIoy8H/AgLIeTq39LTdFkvoXLCnCekv2+wzQL3EOOkCQQDUZakbpisQzeHdFm86n6ehUmkojHth8NN6t/2TPW2oJbqXjzbAIgyF/G2XIr+mNnieL6fUqDx6PyVJc4HN01HfAkAJqH8IgQLFdIul5e1jzZ1BEjxDykGM4B1lN25R+NHKT+hpEcbZqF6qPnsn+pRPQ9EyqTLG5EZjZRhrAnToBg7pAkBYykrgkR2tv7OtHgTPqBCGoxHs7pVhwzBVO/dYhzSBN4yCcU89EL7VgEo8BT3C+UCBOIDbqJznqeAnjY71AWNXAkBlf5wKS+iM4uGjN+hn2wcT24GJFERHKPlcjrLdnT9kWw785hn+8m4ZKMJwhm1YrsnT86ty4ek63t1Wf2vJET4y";

	//新版在线支付私钥
	public static String private_key_v2 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7iVeHIgtLnazNjurEsZ88H1gwqaNOalStQvJhr/+byLea5BU7YdAa4ti13oXWASb01wvqPHcE7kU6fcel40pSwa4CfQTk12VhwXf8Oq1KYBjG1WDRiVEj8cs4P5vcRKJWYDypZd2idD8ycNffsaHv7gacHCLIry28d1I78HHjm/esgqGLCy6feCryYVBr9ESM5qN11Lv4BSjxMSu+tQ0vwjCxgAdG3TJvutLLAyki750FlNHmL9k3oC76gzRDWqUbrrykLCeGwvWgrkHMibJ/yix81o9vyRVzE/JgmANNOtxuKIUtfOXSYE3HsnTjSHYucslmqleEP9w8vk+CsqMHAgMBAAECggEBAISDE1nk8F5J16SX68N4Tq/I5iPcegwajiKvP11PYynMtg+4QlhnUQjuaXp49dC1l7VBjqXAe8j8I+akocHRzN6VBEO12xNoL7bXYdTUEUaQiHFWrMbiZHcljxb7u0H1LVAjSDnaRLVZtp4Jpj/l4CsM4ZbFOr7bKVIWbgD0cUUF5ih7hM1ZvJanNXWMnIixOk3Ool1AzqpUUWRO7gU76mmHva9L48tX35/p0z2PrH407Cq64B1/5Nod5lyKo2+ajmuhFrQQ1UNxsNpvwlg/i7RcjVepY7EVIij97+rW7pVaYTZTXJgSpSLKY8hO0qf7fqFA33XHOJ7vaky/TPJzm4ECgYEA452F69GPgp0S6KYUNBnzjV3l0MvK2p7OQFnsfdknOARZ6922hYDAYGbWWU+PWR+9pV4llRLNCqWi2kgsNVzWack8bZthLJZyRSr6lf+yBESBUpsX9+Jwj/Pif0w/27+8IBQFEEBlUV9IF0eUlQq+kDC6xNQFZeiEUIAz4nercFcCgYEA0uxTvSClvesn9i57UJwjJpfhC7ztcZKw+QoTgnda7lNxEvfd1WikwOusZrCSx/9LpjyRkCUMa9j6hRaMr4wbvM12eaOsb2IdDeK8mbCSZsN0Lss38196sKCPlCfrqUPPUqB55tUU8Rglr/7Ur80jgGw9y+IPBxy3Z2d8a07/9NECgYALSZqEkUXeok93vhSuaMMNNaTZ7+FFai6sPkleDFDHlF+pNLuCb1oa7b6fezSOpOZQtxSCCgalCoXC8WVP2CTB5jra7KOrBGLyTylSGvYHBCatpAdSQaZ3XQ7UZUGdciqwJI4Duk3L9T+r997EbV355JC+hg0meptlKpZoGqsjzQKBgG0+qkCS4EV1vsTrXkNqG2qpz43c1L62U6MpbOFuIFLANRi1+NfH0014w1qMqfmcaPo49MvL5JsXTuoPu95Qmy023d4yv+UQ1CuU/Eo+AhXntg1mhBwxn8JL5xG9e3m3/XG2XG0KgEY/U4XMXyyO+4xQg9FNzKrKXNPZ7b9Gs5uBAoGBALyM491LPw+gwbVGrqZiJ2oKfKP08brqFLaZUog9Sh7I+brN8zuFiRbHi4EpXBEOKRsWqjrDNe3nBCcXcwZEhTtVKBPVXehPNKS31bjUF2Bxbpw49D6aUruBHW2YvgWwIMYnaxAHnLZVyLn2Rj+KSf804TDSZWoNk9wlMiI/tWxJ";
}
