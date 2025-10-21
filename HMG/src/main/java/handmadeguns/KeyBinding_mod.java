package handmadeguns;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

import static handmadeguns.HandmadeGunsCore.HMG_proxy;

public class KeyBinding_mod {
	public static ArrayList<KeyBinding_mod> TrackedKeyBinding = new ArrayList<KeyBinding_mod>();
	public KeyBinding keyBinding;
	public boolean stopper;
	public KeyBinding_mod(String p_i45001_1_, int p_i45001_2_, String p_i45001_3_){
		keyBinding = new KeyBinding(p_i45001_1_,p_i45001_2_,p_i45001_3_);
		stopper = false;

		TrackedKeyBinding.add(this);
	}

	public boolean isKeyDown_withStopper(){
//		System.out.println("debug\t" + this.keyBinding.getKeyDescription());
//		System.out.println("stopper\t" + stopper);
//		System.out.println("current\t" + keyDown(keyBinding));
		boolean flag = keyDown(keyBinding);
		boolean returnVal = flag && !stopper;
		stopper = flag;
		return returnVal;
	}

	public boolean isKeyDown_noStop(){
		//what does this fucking do, why is it called no stop????
		return keyDown(keyBinding);
	}

	boolean toggle = false;
	public boolean isKeyDown_toggle(){
		if(this.isKeyDown_withStopper())toggle = !toggle;
		return toggle;
	}

	public boolean toggleState(){
		return toggle;
	}

	public boolean includeNull = false;
	public boolean keyDown(KeyBinding key)
	{
		boolean state = includeNull && (key.getKeyCode() == Keyboard.KEY_NONE);
		if(includeNull) {
//			System.out.println("debug\t\t" + this.keyBinding.getKeyDescription());
//			System.out.println("includeNull\t" + includeNull);
//			System.out.println("keycode\t\t" + key.getKeyCode());
//			System.out.println("state\t\t\t" + state);
		}
		if (HMG_proxy.getMCInstance().currentScreen == null || HMG_proxy.getMCInstance().currentScreen.allowUserInput) {
			state |= key.getIsKeyPressed();
		}else {
			state = false;
		}
		return state;
	}

}
