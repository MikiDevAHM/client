package me.eldodebug.soar.management.security;

import com.glideclient.Glide;

public class SecurityFeature {

	public SecurityFeature() {
		Glide.getInstance().getEventManager().register(this);
	}
}
