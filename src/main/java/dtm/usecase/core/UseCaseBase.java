package dtm.usecase.core;

public abstract class UseCaseBase {
    public UseCaseBase(){}

    protected void throwsMethod() throws Exception{
        throw new RuntimeException();
    }

    protected void throwsMethod(String msg) throws Exception{
        throw new RuntimeException(msg);
    }

    protected void throwsMethod(Throwable th) throws Exception{
        throw new RuntimeException(th);
    }

    protected void throwsMethod(String msg, Throwable th) throws Exception{
        throw new RuntimeException(msg, th);
    }
}
