package dtm.usecase.core;

public class UseCaseException extends RuntimeException{
    
    public UseCaseException(String msg){
        super(msg);
    }

    public UseCaseException(Throwable th){
        super(th);
    }
}
