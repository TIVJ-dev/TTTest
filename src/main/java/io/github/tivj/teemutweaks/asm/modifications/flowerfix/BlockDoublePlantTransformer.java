package io.github.tivj.teemutweaks.asm.modifications.flowerfix;

import io.github.tivj.teemutweaks.asm.tweaker.transformer.ITransformer;
import io.github.tivj.teemutweaks.utils.BytecodeHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

public class BlockDoublePlantTransformer implements ITransformer {
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.block.BlockDoublePlant"};
    }

    public static FieldNode variantMap = new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "variantMap", "Ljava/util/HashMap;", null, null); //TODO: probably also a good idea to also clear this map if the setting is turned off

    @Override
    public void transform(ClassNode classNode, String name) {
        classNode.fields.add(variantMap);
        for (MethodNode methodNode : classNode.methods) {
            String methodName = mapMethodName(classNode, methodNode);
            if (methodName.equals("getActualState") || methodName.equals("func_176221_a")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                LabelNode labelToEnd = null;
                LabelNode labelToMapGetter = new LabelNode();
                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (labelToEnd == null) {
                        if (node.getOpcode() == Opcodes.IF_ACMPNE) {
                            labelToEnd = ((JumpInsnNode)node).label;
                        }
                    } else {
                        if (node.getOpcode() == Opcodes.IF_ACMPNE && ((JumpInsnNode)node).label.equals(labelToEnd)) {
                            ((JumpInsnNode) node).label = labelToMapGetter;
                        } else if (node.getOpcode() == Opcodes.ASTORE && node.getPrevious().getOpcode() == Opcodes.INVOKEINTERFACE) {
                            String invokeName = mapMethodNameFromNode(node.getPrevious());
                            if (((VarInsnNode) node).var == 1 && (invokeName.equals("withProperty") || invokeName.equals("func_177226_a"))
                            ) {
                                methodNode.instructions.insert(node, getVariant(labelToEnd, labelToMapGetter));
                                break;
                            }
                        }
                    }
                }
                BytecodeHelper.printEmAll(methodNode.instructions);
            }
        }
    }


    private InsnList getVariant(LabelNode labelToEnd, LabelNode labelToMapGetter) {
        InsnList list = new InsnList();
        list.add(new JumpInsnNode(Opcodes.GOTO, labelToEnd));

        list.add(labelToMapGetter);
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/block/BlockDoublePlant", variantMap.name, variantMap.desc));
        list.add(new JumpInsnNode(Opcodes.IFNONNULL, labelToEnd)); // the map is never initialized, so the code below will never run, but it's presence breaks stuff

        //the issue is caused by the lines below
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/block/BlockDoublePlant", "field_176493_a", "Lnet/minecraft/block/properties/PropertyEnum;")); // VARIANT
            list.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/block/BlockDoublePlant", variantMap.name, variantMap.desc));
            list.add(new VarInsnNode(Opcodes.ALOAD, 3));
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false));
            list.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Comparable"));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", "func_177226_a", "(Lnet/minecraft/block/properties/IProperty;Ljava/lang/Comparable;)Lnet/minecraft/block/state/IBlockState;", true)); // withProperty
        list.add(new VarInsnNode(Opcodes.ASTORE, 1));
        //the issue is caused by the lines above

        //`ACONST_NULL` and
        //`ASTORE 1` do work for some reason.
        return list;
    }
}