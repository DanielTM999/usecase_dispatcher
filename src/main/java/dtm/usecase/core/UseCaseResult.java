package dtm.usecase.core;


import java.util.function.Supplier;

public abstract class UseCaseResult {
   public abstract <T> T get();
   public abstract <x extends Throwable> UseCaseResult ifThrow(Supplier<? extends x> exception) throws x;
}
