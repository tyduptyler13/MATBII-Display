package com.myuplay.matb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A much fancier container for handling EventContainers and iterating through
 * them.
 * 
 * Essentially a customized list implementation.
 * 
 * This list is for the stats module and will create a custom filter on the
 * data.
 * 
 * @author Tyler
 * 
 */
public class ECList implements Iterable<EventContainer> {

	private final List<EventContainer> list;

	/**
	 * This creates a filtered list and generates all needed variables for
	 * handling stats.
	 * 
	 * @param masterList
	 */
	public ECList(List<EventContainer> masterList) {

		List<EventContainer> tmp = new ArrayList<EventContainer>();

		for (EventContainer event : masterList) {

			if (!event.hasMATB())
				continue;

			if (event.matb.event.matches("(Resource Management|System Monitoring|Communications|Tracking)")
					&& (event.matb.eventType == MATBEvent.EventType.SubjectResponse
					|| (event.matb.event.equals("Tracking")))) {

				tmp.add(event);

			}

		}

		list = tmp; //For debugging.

	}

	@Override
	public SuperIterator iterator() {
		return new SuperIterator();
	}

	/**
	 * This is an implementation that allows more random access to a list
	 * and includes additional functions to access specificly {@link TRCKEvent}s.
	 * @author Tyler
	 *
	 */
	public class SuperIterator extends PeekingIterator<EventContainer>{

		private int index;
		private int trackIndex;
		private final List<Integer> trackList;

		/**
		 * Starts the iterator before the list.
		 * {@link SuperIterator#next()} will get the first position.
		 * 
		 */
		public SuperIterator(){
			index = -1;
			trackIndex = -1;

			List<Integer> tmp = new ArrayList<Integer>();

			for (int i = 0; i < list.size(); ++i){
				if (list.get(i).hasTRCK()){
					tmp.add(i);
				}
			}

			trackList = tmp; //For debugging.

		}

		public SuperIterator(SuperIterator s){
			index = s.index;
			trackIndex = s.trackIndex;
			trackList = s.trackList; //No need for deep copy.
		}

		@Override
		public boolean hasNext() {
			if (index + 1 >= list.size())
				return false;
			return true;
		}

		/**
		 * Returns true if there is still a {@link TRCKEvent} remaining.
		 * @return
		 */
		public boolean hasNextTRCK() {
			if (trackIndex + 1 >= trackList.size())
				return false;
			return true;
		}

		@Override
		public EventContainer next() {
			if (hasNext()){
				index++;
				EventContainer e = list.get(index);

				if (e.hasTRCK()){

					trackIndex = trackList.indexOf(index); //Find the index of this index value.

				}

				return e;
			} else {
				return null;
			}
		}

		/**
		 * Will retrieve the next {@link TRCKEvent} and move the
		 * index to where the next tracking event is.
		 * 
		 * Returns null otherwise.
		 * @return TRCKEvent or null
		 */
		public TRCKEvent nextTRCK(){

			if (hasNextTRCK()){

				trackIndex++;

				index = trackList.get(trackIndex);

				return list.get(index).trck;

			} else {
				return null;
			}

		}

		/**
		 * Disabled and will do nothing.
		 */
		@Deprecated
		public void remove() {}

		@Override
		public boolean hasPrevious() {
			return (index - 1 >= 0);
		}

		public boolean hasPreviousTRCK(){
			return (trackIndex - 1 >= 0);
		}

		@Override
		public EventContainer peek() {
			return list.get(index + 1);
		}

		public TRCKEvent peekTRCK(){
			if (hasNextTRCK()){
				return list.get(trackList.get(trackIndex+1)).trck;
			} else {
				return null;
			}
		}

		@Override
		public EventContainer peek(int i) {
			return list.get(index + i);
		}

		public EventContainer peekPrevious(){
			return peek(-1);
		}

		public TRCKEvent peekTRCK(int i){

			if (hasTRCK(i)){
				return list.get(trackList.get(trackIndex + i)).trck;
			} else {
				return null;
			}

		}

		public TRCKEvent peekPreviousTRCK(){
			return peekTRCK(-1);
		}

		public boolean isNextTracking(){
			if (hasNext()){
				return peek().hasTRCK();
			} else {
				return false;
			}
		}

		@Override
		public boolean has(int i) {
			int tmp = index + i;
			return (tmp >= 0 && tmp < list.size());
		}

		public boolean hasTRCK(int i){
			int tmp = trackIndex + i;
			return (tmp >= 0 && tmp < trackList.size());
		}

		@Override
		public EventContainer previous() {
			if (hasPrevious()){
				index--;
				EventContainer e = list.get(index);

				if (e.hasTRCK()){
					trackIndex = trackList.indexOf(index);
				}

				return e;

			} else {
				return null;
			}
		}

		public TRCKEvent previousTRCK() {
			if (hasPreviousTRCK()){
				trackIndex--;

				index = trackList.get(trackIndex);

				return list.get(index).trck;

			} else {
				return null;
			}
		}

		@Override
		public int nextIndex() {
			return index + 1;
		}

		public int nextTRCKIndex() {
			return trackList.get(trackIndex + 1);
		}

		public int index(){
			return index;
		}

		public int TRCKIndex(){
			return trackList.get(trackIndex);
		}

		@Override
		public int previousIndex() {
			return index - 1;
		}

		public int previousTRCKIndex() {
			return trackList.get(trackIndex - 1);
		}

		public SuperIterator clone(){
			return new SuperIterator(this);
		}

		@Deprecated
		public void set(EventContainer e) {}

		@Deprecated
		public void add(EventContainer e) {}

	}

}
