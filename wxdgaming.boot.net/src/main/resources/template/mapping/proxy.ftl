public void proxy(Object out, Object instance, Object[] params) throws Exception {
    AtomicReference atomicReference = (AtomicReference) out;
    ${ret_type} ret = null;
    ${ret_set}((${instanceClass}) instance).${methodName}(
        <#list paramTypes as paramType>
        ${paramType}
        </#list>
    );
    atomicReference.set(ret);
}