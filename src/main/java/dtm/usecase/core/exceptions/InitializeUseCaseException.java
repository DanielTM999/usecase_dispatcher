package dtm.usecase.core.exceptions;

public class InitializeUseCaseException extends RuntimeException{
    public InitializeUseCaseException(String msg){
        super(msg);
    }

    public InitializeUseCaseException(Throwable th){
        super(th);
    }
}
