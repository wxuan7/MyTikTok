package com.whensunset.invoker.model

class InvokerInfos {
    Map<String, File> mClassContainers = new HashMap<>()
    Set<Invocation> mInvocations = new HashSet<>()

    public File fileForClass(String className) {
        return mClassContainers.get(className)
    }
}