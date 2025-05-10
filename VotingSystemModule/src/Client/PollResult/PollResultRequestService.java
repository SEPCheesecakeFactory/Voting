package Client.PollResult;

import Common.PollResult;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface PollResultRequestService
{
  void getResult(PollResult pollResult);
  void sendResultRequest(int pollID);
  void addPropertyChangeListener(PropertyChangeListener listener);
  void addPropertyChangeListener(String name, PropertyChangeListener listener);
  void removePropertyChangeListener(PropertyChangeListener listener);
  void removePropertyChangeListener(String name,
      PropertyChangeListener listener);
  void propertyChange(PropertyChangeEvent evt);

}
