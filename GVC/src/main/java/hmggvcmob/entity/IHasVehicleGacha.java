package hmggvcmob.entity;

import handmadevehicle.VehicleSpawnGachaOBJ;

import java.util.ArrayList;

public interface IHasVehicleGacha {
	ArrayList<VehicleSpawnGachaOBJ> getVehicleGacha();
	int getVehicleGacha_rate_sum();
	void setVehicleName(String string);
}
