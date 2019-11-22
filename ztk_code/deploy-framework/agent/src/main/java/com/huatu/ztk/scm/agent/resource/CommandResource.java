package com.huatu.ztk.scm.agent.resource;

import com.huatu.ztk.scm.common.Paths;
import com.huatu.ztk.scm.common.ShellExec;
import org.apache.zookeeper.ZookeeperUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-16
 * Time: 下午4:43
 * To change this template use File | Settings | File Templates.
 */
@Path("/cmd")
@Produces(MediaType.TEXT_PLAIN)
public class CommandResource {
  protected final static Logger logger = LoggerFactory.getLogger(CommandResource.class);

  @GET
  @Path("jps")
  public String jps() {
    return ShellExec.exec( Paths.BIN_HOME + "/cmd_jps.sh",null)[1];
  }

  @GET
  @Path("ps")
  public String ps() {
    return ShellExec.exec(Paths.BIN_HOME + "/cmd_ps.sh",null)[1];
  }

  @GET
  @Path("top")
  public String top() {
    return ShellExec.exec( Paths.BIN_HOME + "/cmd_top.sh",null)[1];
  }

  @GET
  @Path("log")
  public String log() {
    logger.debug("debug");
    logger.info("info");
    logger.warn("warn");
    logger.error("error");
    return "ok";
  }


  @GET
  @Path("ping")
  public String ping() {
    boolean zkStarted = ZookeeperUtil.getZkClient().isStarted();
    boolean zkConn = ZookeeperUtil.getZkClient().getZookeeperClient().isConnected();
    JSONObject json = new JSONObject();
    try {
      json.put("zkStarted", zkStarted);
      if (!zkStarted||!zkConn) {
        json.put("msg", "agent 与zookeeper连接丢失");
      } else {
        try {
          ZookeeperUtil.get("/huatu-scm/init");
        } catch (Exception e) {
          json.put("zkStarted", false);
          json.put("msg", "agent 与zookeeper连接异常");
        }
      }
    } catch (JSONException e) {
      logger.error("JSONException", e);
    }
    return json.toString();
  }

}
