package me.logwet.noverworld.mixin.common;

import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.InputStream;

@Mixin(StructureManager.class)
public interface StructureManagerInvoker {
    @Invoker("readStructure")
    public Structure invokeReadStructure(InputStream structureInputStream);
}
