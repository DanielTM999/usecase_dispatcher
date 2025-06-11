package dtm.usecase.core;

import dtm.usecase.exceptions.UseCaseRuntimeException;

public abstract class UseCaseBase {

    public UseCaseBase(){}

    protected void throwsMethod(){
        throw new UseCaseRuntimeException("unknow interrupt");
    }

    protected void throwsMethod(String msg){
        throw new UseCaseRuntimeException(msg);
    }

    protected void throwsMethod(Throwable th) {
        throw new UseCaseRuntimeException(th);
    }

    protected void throwsMethod(String msg, Throwable th){
        throw new UseCaseRuntimeException(msg, th);
    }

}
