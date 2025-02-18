package dtm.usecase.core;

public final class PidUseCaseResult {
    private String getPid;
    private Class<? extends UseCaseBase> referenceClass;

    public PidUseCaseResult(String getPid, Class<? extends UseCaseBase> referenceClass) {
        this.getPid = getPid;
        this.referenceClass = referenceClass;
    }

    public String getGetPid() {
        return getPid;
    }

    public Class<? extends UseCaseBase> getReferenceClass() {
        return referenceClass;
    }

    
}
