package com.huatu.hadoop.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

    private static String encoding = "QURNSU46S1lMSU4=";
    private static final String baseURL = "http://192.168.100.26:7070/kylin/api";

    public static String login(String user, String passwd) {
        String method = "POST";
        String para = "/user/authentication";
        byte[] key = (user + ":" + passwd).getBytes();
        encoding = Base64.encodeBase64String(key);
        return excute(para, method, null);

    }


    public static String listQueryableTables(String projectName) {

        String method = "GET";
        String para = "/tables_and_columns?project=" + projectName;

        return excute(para, method, null);


    }

    /**
     * @param offset      required int Offset used by pagination
     * @param limit       required int Cubes per page.
     * @param cubeName    optional string Keyword for cube names. To find cubes whose name contains this keyword.
     * @param projectName optional string Project name.
     * @return
     */

    public static String listCubes(int offset,
                                   int limit,
                                   String cubeName,
                                   String projectName) {
        String method = "GET";
        String para = "/cubes?offset=" + offset
                + "&limit=" + limit
                + "&cubeName=" + cubeName
                + "&projectName=" + projectName;
        return excute(para, method, null);

    }

    /**
     * *
     * * @param cubeName  Cube name.
     * * @return
     */
    public static String getCubeDes(String cubeName) {
        String method = "GET";
        String para = "/cube_desc/" + cubeName;
        return excute(para, method, null);


    }

    /**
     * *
     * * @param cubeName
     * * @return
     */

    public static String getCube(String cubeName) {
        String method = "GET";
        String para = "/cubes/" + cubeName;
        return excute(para, method, null);


    }

    /**
     * *
     * * @param modelName Data model name, by default it should be the same with cube name.
     * * @return
     */

    public static String getDataModel(String modelName) {
        String method = "GET";
        String para = "/model/" + modelName;
        return excute(para, method, null);

    }

    /**
     * @param cubeName cubeName Cube name.
     * @return
     */
    public static String enableCube(String cubeName) {

        String method = "PUT";
        String para = "/cubes/" + cubeName + "/enable";
        return excute(para, method, null);


    }

    /**
     * @param cubeName Cube name.
     * @return
     */
    public static String disableCube(String cubeName) {

        String method = "PUT";
        String para = "/cubes/" + cubeName + "/disable";
        return excute(para, method, null);


    }

    /**
     * @param cubeName Cube name.
     * @return
     */
    public static String purgeCube(String cubeName) {

        String method = "PUT";
        String para = "/cubes/" + cubeName + "/purge";
        return excute(para, method, null);


    }

    /**
     * @param jobId Job id
     * @return
     */
    public static String resumeJob(String jobId) {

        String method = "PUT";
        String para = "/jobs/" + jobId + "/resume";
        return excute(para, method, null);


    }


    /**
     * startTime - required long Start timestamp of data to build, e.g. 1388563200000 for 2014-1-1
     * endTime - required long End timestamp of data to build
     * buildType - required string Supported build type: ‘BUILD’, ‘MERGE’, ‘REFRESH’
     *
     * @param cubeName Cube name.
     * @return
     */
    public static String buildCube(String cubeName, String body) {
        String method = "PUT";
        String para = "/cubes/" + cubeName + "/rebuild";

        return excute(para, method, body);
    }

    /***
     * @param jobId  Job id.
     * @return 5
     * */
    public static String discardJob(String jobId) {

        String method = "PUT";
        String para = "/jobs/" + jobId + "/cancel";
        return excute(para, method, null);


    }

    /**
     * @param jobId Job id.
     * @return
     */
    public static String getJobStatus(String jobId) {

        String method = "GET";
        String para = "/jobs/" + jobId;
        return excute(para, method, null);


    }

    /**
     * @param jobId  Job id.
     * @param stepId Step id; the step id is composed by jobId with step sequence id;
     *               for example, the jobId is “fb479e54-837f-49a2-b457-651fc50be110”, its 3rd step id
     *               is “fb479e54-837f-49a2-b457-651fc50be110-3”,
     * @return
     */
    public static String getJobStepOutput(String jobId, String stepId) {
        String method = "GET";
        String para = "/" + jobId + "/steps/" + stepId + "/output";
        return excute(para, method, null);

    }

    /**
     * @param tableName table name to find.
     * @return
     */
    public static String getHiveTable(String tableName) {
        String method = "GET";
        String para = "/tables/" + tableName;
        return excute(para, method, null);
    }

    /**
     * * @param tableName  table name to find.
     *
     * @return
     */
    public static String getHiveTableInfo(String tableName) {
        String method = "GET";
        String para = "/tables/" + tableName + "/exd-map";
        return excute(para, method, null);

    }

    /**
     * @param projectName will list all tables in the project.
     * @param extOptional boolean set true to get2Version extend info of table.
     * @return
     */


    public static String getHiveTables(String projectName, boolean extOptional) {
        String method = "GET";
        String para = "/tables?project=" + projectName + "&ext=" + extOptional;
        return excute(para, method, null);

    }

    /**
     * @param tables  table names you want to load from hive, separated with comma.
     * @param project the project which the tables will be loaded into.
     * @return
     */


    public static String loadHiveTables(String tables, String project) {
        String method = "POST";
        String para = "/tables/" + tables + "/" + project;
        return excute(para, method, null);

    }

    /**
     * @param type   ‘METADATA’ or ‘CUBE’
     * @param name   Cache key, e.g the cube name.
     * @param action ‘create’, ‘update’ or ‘drop’
     * @return
     */


    public static String wipeCache(String type, String name, String action) {
        String method = "POST";
        String para = "/cache/" + type + "/" + name + "/" + action;
        return excute(para, method, null);

    }

    public static String query(String body) {
        String method = "POST";
        String para = "/query";

        return excute(para, method, body);

    }


    public static String excute(String para, String method, String body) {

        StringBuilder out = new StringBuilder();
        try {
            URL url = new URL(baseURL + para);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            connection.setRequestProperty("Content-Type", "application/json");
            if (body != null) {
                byte[] outputInBytes = body.getBytes("UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(outputInBytes);
                os.flush();

                os.close();
            }
            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                out.append(line);

            }
            in.close();
            connection.disconnect();


        } catch (Exception e) {
            e.printStackTrace();

        }
        return out.toString();

    }

    /**
     * @param para
     * @param body
     * @return
     */
    public static String postExecute(String para, String body) {

        StringBuilder out = new StringBuilder();
        try {

            String url = baseURL + para;

            byte[] requestBytes = body.getBytes("utf-8");

            HttpClient httpClient = new HttpClient();

            PostMethod postMethod = new PostMethod(url);

            postMethod.setRequestHeader("Authorization", "Basic " + encoding);//Soap Action Header!

            InputStream inputStream = new ByteArrayInputStream(requestBytes, 0, requestBytes.length);
            RequestEntity requestEntity = new InputStreamRequestEntity(inputStream, requestBytes.length, "application/json");
            postMethod.setRequestEntity(requestEntity);

            int state = httpClient.executeMethod(postMethod);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(postMethod.getResponseBodyAsStream()));

            String responseLine = "";
            while ((responseLine = bufferedReader.readLine()) != null) {
                out.append(responseLine);
            }


        } catch (Exception e) {
            e.printStackTrace();

        }
        return out.toString();

    }


    /**
     * @param para
     * @param body
     * @return
     */
    public static String putExecute(String para, String body) {

        StringBuilder out = new StringBuilder();
        try {

            String url = baseURL + para;

            byte[] requestBytes = body.getBytes("utf-8");

            HttpClient httpClient = new HttpClient();
            PutMethod putMethod = new PutMethod(url);

            putMethod.setRequestHeader("Authorization", "Basic " + encoding);//Soap Action Header!

            InputStream inputStream = new ByteArrayInputStream(requestBytes, 0, requestBytes.length);
            RequestEntity requestEntity = new InputStreamRequestEntity(inputStream, requestBytes.length, "application/json");
            putMethod.setRequestEntity(requestEntity);


            int state = httpClient.executeMethod(putMethod);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(putMethod.getResponseBodyAsStream()));

            String responseLine = "";
            while ((responseLine = bufferedReader.readLine()) != null) {
                out.append(responseLine);
            }


        } catch (Exception e) {
            e.printStackTrace();

        }
        return out.toString();

    }

    /**
     * @param para
     * @param body
     * @return
     */
    public static String getExecute(String para, String body) {

        StringBuilder out = new StringBuilder();
        try {

            String url = baseURL + para;


            HttpClient httpClient = new HttpClient();

            GetMethod getMethod = new GetMethod(url);

            getMethod.setRequestHeader("Authorization", "Basic " + encoding);//Soap Action Header!

            int state = httpClient.executeMethod(getMethod);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getMethod.getResponseBodyAsStream()));

            String responseLine = "";
            while ((responseLine = bufferedReader.readLine()) != null) {
                out.append(responseLine);
            }


        } catch (Exception e) {
            e.printStackTrace();

        }
        return out.toString();

    }


    public static void main(String[] args) throws IOException {


        StringBuilder out = new StringBuilder();
        try {

            String url = "http://123.103.86.52:10917/c/v1/report/playBack";

            byte[] requestBytes = "{\"syllabusId\":1,\"username\":\"213a\"}".getBytes("utf-8");

            HttpClient httpClient = new HttpClient();

            PostMethod postMethod = new PostMethod(url);


            InputStream inputStream = new ByteArrayInputStream(requestBytes, 0, requestBytes.length);
            RequestEntity requestEntity = new InputStreamRequestEntity(inputStream, requestBytes.length, "application/json");
            postMethod.setRequestEntity(requestEntity);

            int state = httpClient.executeMethod(postMethod);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(postMethod.getResponseBodyAsStream()));

            String responseLine = "";
            while ((responseLine = bufferedReader.readLine()) != null) {
                out.append(responseLine);
            }


        } catch (Exception e) {
            e.printStackTrace();

        }
        System.out.println(out.toString());
    }

}
