package me.logwet.noverworld.mixin.common;

import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.recipe.book.RecipeBookOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBook.class)
public interface RecipeBookAccessor {
    @Accessor("options")
    RecipeBookOptions getOptionsField();
}
