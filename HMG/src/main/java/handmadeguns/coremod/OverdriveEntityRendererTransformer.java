package handmadeguns.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.List;

public class OverdriveEntityRendererTransformer implements IClassTransformer, Opcodes {
    private static final String ENTITY_RENDERER = "net.minecraft.client.renderer.EntityRenderer";
    private static final String ENTITY_RENDERER_OBF = "blt";
    private static final String NET_HANDLER_PLAY_CLIENT = "net.minecraft.client.network.NetHandlerPlayClient";
    private static final String CONTROLLER = "handmadeguns/client/camera/OverdriveCameraController";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        boolean entityRenderer = ENTITY_RENDERER.equals(transformedName) || ENTITY_RENDERER.equals(name) || ENTITY_RENDERER_OBF.equals(name);
        boolean netHandler = NET_HANDLER_PLAY_CLIENT.equals(transformedName) || NET_HANDLER_PLAY_CLIENT.equals(name);
        if (!entityRenderer && !netHandler) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(classNode, 0);

        int patches = 0;
        List<MethodNode> methods = classNode.methods;
        for (MethodNode method : methods) {
            if (entityRenderer) {
                if (isUpdateCameraAndRender(method)) {
                    patches += injectUpdate(method);
                } else if (isOrientCamera(method)) {
                    patches += injectOrient(method);
                } else if (isApplyBobbing(method)) {
                    patches += injectApplyBobbing(method);
                } else if (isGetFovModifier(method)) {
                    patches += injectFov(method);
                } else if (isHurtCameraEffect(method)) {
                    patches += injectHurt(method);
                }
            } else if (netHandler && isHandleExplosion(method)) {
                patches += injectExplosionPacket(method);
            }
        }

        if (patches == 0) return basicClass;
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private boolean isUpdateCameraAndRender(MethodNode method) {
        return "(F)V".equals(method.desc) && ("updateCameraAndRender".equals(method.name) || "func_78480_b".equals(method.name));
    }

    private boolean isOrientCamera(MethodNode method) {
        return "(F)V".equals(method.desc) && ("orientCamera".equals(method.name) || "func_78467_g".equals(method.name));
    }

    private boolean isApplyBobbing(MethodNode method) {
        return "(F)V".equals(method.desc) && ("applyBobbing".equals(method.name) || "setupViewBobbing".equals(method.name) || "func_78475_f".equals(method.name));
    }

    private boolean isGetFovModifier(MethodNode method) {
        return "(FZ)F".equals(method.desc) && ("getFOVModifier".equals(method.name) || "func_78481_a".equals(method.name));
    }

    private boolean isHurtCameraEffect(MethodNode method) {
        return "(F)V".equals(method.desc) && ("hurtCameraEffect".equals(method.name) || "func_78482_e".equals(method.name));
    }

    private boolean isHandleExplosion(MethodNode method) {
        return method.desc.endsWith(")V")
                && ("handleExplosion".equals(method.name)
                || "func_147283_a".equals(method.name)
                || method.desc.indexOf("S27PacketExplosion") >= 0);
    }

    private int injectUpdate(MethodNode method) {
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(FLOAD, 1));
        hook.add(new MethodInsnNode(INVOKESTATIC, CONTROLLER, "update", "(F)V"));
        method.instructions.insert(hook);
        return 1;
    }

    private int injectOrient(MethodNode method) {
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == RETURN) {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(FLOAD, 1));
                hook.add(new MethodInsnNode(INVOKESTATIC, CONTROLLER, "applyCameraTransforms", "(F)V"));
                method.instructions.insertBefore(insn, hook);
                count++;
            }
        }
        return count > 0 ? 1 : 0;
    }

    private int injectApplyBobbing(MethodNode method) {
        LabelNode vanilla = new LabelNode();
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(FLOAD, 1));
        hook.add(new MethodInsnNode(INVOKESTATIC, CONTROLLER, "applyCustomBobbing", "(F)Z"));
        hook.add(new JumpInsnNode(IFEQ, vanilla));
        hook.add(new org.objectweb.asm.tree.InsnNode(RETURN));
        hook.add(vanilla);
        method.instructions.insert(hook);
        return 1;
    }

    private int injectFov(MethodNode method) {
        int count = 0;
        for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn.getOpcode() == FRETURN) {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(FLOAD, 1));
                hook.add(new VarInsnNode(ILOAD, 2));
                hook.add(new MethodInsnNode(INVOKESTATIC, CONTROLLER, "modifyFov", "(FFZ)F"));
                method.instructions.insertBefore(insn, hook);
                count++;
            }
        }
        return count > 0 ? 1 : 0;
    }

    private int injectHurt(MethodNode method) {
        LabelNode vanilla = new LabelNode();
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(FLOAD, 1));
        hook.add(new MethodInsnNode(INVOKESTATIC, CONTROLLER, "applyHurtCameraEffect", "(F)Z"));
        hook.add(new JumpInsnNode(IFEQ, vanilla));
        hook.add(new org.objectweb.asm.tree.InsnNode(RETURN));
        hook.add(vanilla);
        method.instructions.insert(hook);
        return 1;
    }

    private int injectExplosionPacket(MethodNode method) {
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(ALOAD, 1));
        hook.add(new MethodInsnNode(INVOKESTATIC, CONTROLLER, "handleExplosionPacket", "(Ljava/lang/Object;)V"));
        method.instructions.insert(hook);
        return 1;
    }
}
