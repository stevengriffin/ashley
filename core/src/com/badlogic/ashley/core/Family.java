package com.badlogic.ashley.core;

import java.util.BitSet;

import com.badlogic.ashley.utils.ObjectMap;

/**
 * A family represents a group of components. It is used to describe what entities a system
 * should process. 
 * 
 * Example: {@code Family.getFamilyFor(PositionComponent.class, VelocityComponent.class)}
 * 
 * Families can't be instantiate directly but must be accessed via {@code Family.getFamilyFor()}, this is
 * to avoid duplicate families that describe the same components.
 * 
 * @author Stefan Bachmann
 */
public class Family {
	/** The hashmap holding all families */
	private static ObjectMap<String, Family> families = new ObjectMap<String, Family>();
	private static int familyIndex = 0;
	
	/** Must contain all the components in the set */
	private final BitSet all;
	/** Must contain at least one of the components in the set */
	private final BitSet one;
	/** Cannot contain any of the components in the set */
	private final BitSet exclude;
	/** Each family has a unique index, used for bitmasking */
	private final int index;
	
	/** Private constructor, use static method Family.getFamilyFor() */
	private Family(BitSet all, BitSet any, BitSet exclude){
		this.all = all;
		this.one = any;
		this.exclude = exclude;
		this.index = familyIndex++;
	}
	
	/**
	 * Returns this family's unique index
	 */
	public int getFamilyIndex(){
		return this.index;
	}
	
	/**
	 * Checks if the passed entity matches this family's requirements.
	 * @param entity The entity to check for matching
	 * @return Whether the entity matches or not
	 */
	public boolean matches(Entity entity){
		BitSet entityComponentBits = entity.getComponentBits();
		
		if(entityComponentBits.isEmpty())
			return false;
		
		for (int i = all.nextSetBit(0); i >= 0; i = all.nextSetBit(i+1)){
			if(!entityComponentBits.get(i))
				return false;
		}
		
		if (!one.isEmpty() && !one.intersects(entityComponentBits)) {
			return false;
		}
		
		if (!exclude.isEmpty() && exclude.intersects(entityComponentBits)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns a family with the passed componentTypes as a descriptor. Each set of component types will
	 * always return the same Family instance.
	 * @param componentTypes The components to describe the family, entities must match all these components
	 * @return The family
	 */
	@SafeVarargs
	public static Family getFamilyFor(Class<? extends Component> ...componentTypes){
		return getFamilyFor(ComponentType.getBitsFor(componentTypes), new BitSet(), new BitSet());
	}
	
	/**
	 * Returns a family with the passed componentTypes as a descriptor. Each set of component types will
	 * always return the same Family instance.
	 *  
	 * @param componentTypes The components to describe the family, entities must match all these components. See {@link ComponentType#bitsFor(BitSet, BitSet, BitSet)}.
	 * @return The family
	 */
	@SafeVarargs
	public static Family getFamilyFor(BitSet all, BitSet one, BitSet exclude){
		String hash = getFamilyHash(all, one, exclude);
		Family family = families.get(hash, null);
		if(family == null){
			family = new Family(all, one, exclude);
			families.put(hash, family);
		}
		
		return family;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((all == null) ? 0 : all.hashCode());
		result = prime * result + ((one == null) ? 0 : one.hashCode());
		result = prime * result + ((exclude == null) ? 0 : exclude.hashCode());
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Family))
			return false;
		Family other = (Family) obj;
		if (all == null) {
			if (other.all != null)
				return false;
		} else if (!all.equals(other.all))
			return false;
		if (one == null) {
			if (other.one != null)
				return false;
		} else if (!one.equals(other.one))
			return false;
		if (exclude == null) {
			if (other.exclude != null)
				return false;
		} else if (!exclude.equals(other.exclude))
			return false;
		
		return index == other.index;
	}
	
	private static String getFamilyHash(BitSet all, BitSet one, BitSet exclude) {
		StringBuilder builder = new StringBuilder();
		builder.append("all:");
		builder.append(all.toString());
		builder.append(",one:");
		builder.append(one.toString());
		builder.append(",exclude:");
		builder.append(exclude.toString());
		return builder.toString();
	}
}