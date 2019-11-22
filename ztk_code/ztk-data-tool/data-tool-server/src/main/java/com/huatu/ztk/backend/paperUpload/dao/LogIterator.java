package com.huatu.ztk.backend.paperUpload.dao;

import com.huatu.ztk.backend.paperUpload.bean.PaperUploadError;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lenovo on 2017/6/12.
 */
public class LogIterator {
    private static ThreadLocal<LinkedList<PaperUploadError>> loggerList = ThreadLocal.withInitial(() -> new LinkedList<PaperUploadError>());
    public void setLoggerList(PaperUploadError error){
        LinkedList<PaperUploadError> list = loggerList.get();
        list.add(error);
    }
    public LinkedList<PaperUploadError> getLoggerList(){
        return loggerList.get();
    }
    public void clearLoggerList(){
        LinkedList<PaperUploadError> list = loggerList.get();
        if(!list.isEmpty()){
            loggerList.set(new LinkedList<>() );
        }
    }
    public void setLoggerList(String errorType,String errorMsg,String floor,String location){
        LinkedList<PaperUploadError> list = loggerList.get();
        list.add(PaperUploadError.builder()
                .location(location)
                .floor(floor)
                .errorMsg(errorMsg)
                .errorType(errorType).build());
    }
    public void addLoggerList(List temp){
        loggerList.get().addAll(temp);
    }
    public boolean isLoggerError(String errorFlag) {
        List<PaperUploadError> errorsList = getLoggerList().stream()
                .filter((i)->("error".equals(i.getErrorType())))
                .filter((i)->(errorFlag.equals(i.getErrorFlag())))
                .collect(Collectors.toList());
        if(errorsList.size()>0){
            return true;
        }
        return false;
    }
    public PaperUploadError getSuccessInfo() {
        PaperUploadError temp = getLoggerList().stream()
                .filter((i)->(i.getErrorType().equals("success")))
                .findFirst().orElseGet(null);
        return temp;
    }

}
