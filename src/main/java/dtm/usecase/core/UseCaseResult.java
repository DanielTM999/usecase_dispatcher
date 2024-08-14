package dtm.usecase.core;


import java.util.function.Supplier;

public abstract class UseCaseResult {
   public abstract boolean isDone();
   public abstract String getPID();
   public abstract <T> T get() throws Exception;
   public abstract UseCaseResult ifThrow(Supplier<? extends Exception> exception);
}
