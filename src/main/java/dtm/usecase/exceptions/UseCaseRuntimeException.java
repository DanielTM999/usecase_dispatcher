package dtm.usecase.exceptions;

public class UseCaseRuntimeException extends RuntimeException{

    public UseCaseRuntimeException(String message){
        super(message);
    }

    public UseCaseRuntimeException(String message, Throwable th){
        super(message, th);
    }

    public UseCaseRuntimeException(Throwable th){
        super(th);
    }

}
