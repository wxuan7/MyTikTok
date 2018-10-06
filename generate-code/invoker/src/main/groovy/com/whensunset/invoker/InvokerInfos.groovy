package com.whensunset.invoker

class InvokerInfos {
    Map<String, File> mClassContainers = new HashMap<>()
    Set<Invocation> mInvocations = new HashSet<>()

    public File fileForClass(String className) {
        return mClassContainers.get(className)
    }
}