package com.probridge.vbox.vmm.wmi;

import java.util.Collection;

import com.probridge.vbox.vmm.wmi.utils.VirtualServiceException;


/**
 * To be able to handle {@link VirtualMachine}
 *
 */
public interface VirtualMachineManager {

    /**
     * This method must return all virtual machines that are registered within the server.
     * @return all the registered virtuals
     * @throws VirtualServiceException
     */
    public abstract Collection<? extends VirtualMachine> getVirtualMachines() throws VirtualServiceException;

    /**
     * Returns the virtual machine registered with the name parameter.
     * If a such virtuals isn't registered, an new {@link VirtualServiceException} must be thrown.
     * @param name the registered name of a virtual machine
     * @return the associated virtual machine
     * @throws VirtualServiceException if a such VM doesn't exist or if an issue occurs.
     */
    public abstract VirtualMachine getVMInstance(String name) throws VirtualServiceException;
}
