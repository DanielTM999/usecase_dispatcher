package dtm.usecase.core;

public abstract class UseCaseBase {
    public UseCaseBase(){}
    protected void throwsMethod() throws RuntimeException{
        throw new RuntimeException();
    }
}
