package dtm.usecase.core;

public abstract class UseCaseBase {
    public UseCaseBase(){}

    protected void throwsMethod() throws RuntimeException{
        throw new RuntimeException();
    }

    protected void throwsMethod(String msg) throws RuntimeException{
        throw new RuntimeException(msg);
    }

    protected void throwsMethod(Throwable th) throws RuntimeException{
        throw new RuntimeException(th);
    }

    protected void throwsMethod(String msg, Throwable th) throws RuntimeException{
        throw new RuntimeException(msg, th);
    }
}
