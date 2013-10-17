import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.User;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Scanner;
import org.pircbotx.Colors;
import java.util.Properties;
import java.sql.SQLException;
import java.io.IOException;
import org.joda.time.DateTime;


public class OracleHandler extends ListenerAdapter {


	private PermissionsManager pm;
	private Scanner scanner;
	private String soeapikey;
	private Properties props;

	public OracleHandler(Properties props) {
		super();
		this.pm = PermissionsManager.getInstance();
		this.soeapikey = props.getProperty("soeapikey");
		System.out.println("OracleHandler Initialized.");
		this.props = props;
	}

	public void onMessage(MessageEvent event) {
		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		if (commandLower.startsWith("!oracle ")) {
			User user = event.getUser();
			//if (!pm.isAllowed("!oracle",event.getUser(),event.getChannel())) {
			//	event.respond("Sorry, you are not in the access list for consulting the oracle.");
			//	return;
			//}
			scanner = new Scanner(commandLower);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next();
				
				if (token.equals("list")) {
					if (scanner.hasNext()) {
						token = scanner.next();
						
						if (token.equals("types")) {
							HashMap<Integer,String> hm;
							try {
								hm = Oracle.getTypes(props);
							} catch (SQLException ex) {
								event.respond("Downstream SQL Exception.  Check the logs, sparky.");
								System.out.println(ex);
								ex.printStackTrace();
								return;
							}
							event.respond("Delivering via PM.  If you are planning on running a lot of these, do so in the PM window.");
							event.getBot().sendMessage(user, "Listing Types:");
							SortedSet<Integer> keys = new TreeSet<Integer>(hm.keySet());
							for (Integer key : keys) {
								String type = hm.get(key);
								event.getBot().sendMessage(user, key+":  "+type);
							}
						
						} else if (token.equals("weapons")) {
							if (scanner.hasNextInt()) {
								int type = scanner.nextInt();
								HashMap<Integer,String> hm;
								try {
									hm = Oracle.getWeapons(props,type);
								} catch (SQLException ex) {
									event.respond("Downstream SQL Exception.  Check the logs, sparky.");
									System.out.println(ex);
									ex.printStackTrace();
									return;
								}
								if (hm.size() > 0) {
									event.respond("Delivering via PM. If you are planning on running a lot of these, do so in the PM window.");
									event.getBot().sendMessage(user, "Listing Weapons:");
									SortedSet<Integer> keys = new TreeSet<Integer>(hm.keySet());
									for (Integer key : keys) {
										String weapon = hm.get(key);
										event.getBot().sendMessage(user, key+":  "+weapon);
									}
								} else {
									event.respond("That's not a real type.  Not here.  Not in these parts.");
								}
								//
							} else {
								event.respond("You need a type number. ex. !oracle list weapons 13");
							}
							
						} else if (token.equals("periods")) {
							ArrayList<TimePeriod> al = new ArrayList<TimePeriod>();
							try {
								al = Oracle.getPeriods(props);
							} catch (SQLException ex) {
								event.respond("Downstream SQL Exception.  Check the logs, sparky.");
								System.out.println(ex);
								ex.printStackTrace();
								return;
							}
							event.respond("Delivering via PM.  If you are planning on running a lot of these, do so in the PM window.");
							event.getBot().sendMessage(user, "Listing Periods:");
							for (TimePeriod tp : al) {
								DateTime dt1 = new DateTime(tp.getStart()*1000L);
								DateTime dt2 = new DateTime(tp.getEnd()*1000L);
								event.getBot().sendMessage(user, "id: "+tp.getId()+" start: "+dt1.toString()+" end: "+dt2.toString()+" daily? "+tp.getIsDaily());
							}
							
						}

					} else {
						event.respond("What would you like to list? types, weapons, or periods");
					}
				//END OF 'LIST' HANDLING
				//
				//
				//
				} else if (token.equals("ask")) {
					if (scanner.hasNext("weapon")) {
						scanner.next();
						if (scanner.hasNextInt()) {
							int id = scanner.nextInt();
							HashMap<String,String> hm=new HashMap<String,String>(); 
							try {
								if (scanner.hasNextInt()) {
									int period = scanner.nextInt();
									hm = Oracle.getKillAggregate(props,id,period);
								} else {
									hm = Oracle.getKillAggregate(props,id);
								}
							} catch (SQLException ex) {
								event.respond("Downstream SQL Exception.  Here's the gritty:");
								event.respond(ex.getMessage());
								return;
							}
							if (hm.size()>0) {
								//event.respond("Prepare for Stats:");
								event.respond(hm.get("name")+" -  kills: "+hm.get("kills")+
											" uniques: "+hm.get("uniques")+
											" kpu: "+hm.get("kpu")+" avgbr: "+hm.get("avgbr")+
											" q1kpu: "+hm.get("q1kpu")+" q2kpu: "+hm.get("q2kpu")+
											" q3kpu: "+hm.get("q3kpu")+" q4kpu: "+hm.get("q4kpu"));


							} else {
								event.respond("Was that a legit weapon id and period?  I didn't get anything back.");
							}
						} else {
							event.respond("Need a weapon id and optionally a period.");
						}
					
					} else if (scanner.hasNext("type")) {
						//blah
					} else {
						event.respond("What, like, just in general? Try !oracle ask <weapon/type> [period]");
					}
				} else if (token.equals("dump")) {
					if (scanner.hasNext("weapon")) {
						scanner.next();
						if (scanner.hasNextInt()) {
							int id = scanner.nextInt();
							try {
								ArrayList<KillAggregateRow> al = Oracle.getAllKillAggregates(props, id);
								int rows = al.size();
								if (rows < 1) {
									event.respond("Was that a legit weapon id?  I didn't get anything back.");
								} else {
									int startPeriod = al.get(0).getPeriod();
									int endPeriod = al.get(rows-1).getPeriod();
									String name = al.get(0).getName();
									String pasteTitle = id+" "+name+" periods "+startPeriod+"-"+endPeriod;
									String pasteBody = new String(pasteTitle);
									pasteBody += "\r\n";
									pasteBody += "period, kills, uniques, kpu, avgbr, q1kpu, q2kpu, q3kpu, q4kpu\r\n";
									for (KillAggregateRow kar : al) {
										pasteBody += kar.getPeriod()+","+
												kar.getKills()+","+
												kar.getUniques()+","+
												kar.getKpu()+","+
												kar.getAvgbr()+","+
												kar.getQ1kpu()+","+
												kar.getQ2kpu()+","+
												kar.getQ3kpu()+","+
												kar.getQ4kpu()+"\r\n";
									}
									try {
										String url = PastebinHelper.postString(pasteTitle, pasteBody, props);
										event.respond("Your results: "+url);
										return;
									}catch (Exception e) {
										event.respond("Downstream IO Exception.  Get maradine.  Here's the gritty:");
										event.respond(e.getMessage());
										return;
									}

								}




							} catch (SQLException ex) {
								event.respond("Downstream SQL Exception.  Get maradine.  Here's the gritty:");
								event.respond(ex.getMessage());
								return;
							}
						} else {
							event.respond("I need a weapon id - !oracle dump weapon <id>");
						}

					} else {
						event.respond("Right now I can only dump weapons - !oracle dump weapon <id>");
					}
				}

				//END OF 'STATS' HANDLING
				//
				//
				//


				else if (token.equals("some other thing")) {
					//blah
				} else {
					event.respond("I don't know how to do that.  'ask', 'list', and 'dump' are helpful.");
				}

				
			}
		}
		
		if (command.equals("!oracle")) {
	
			event.respond("What?");
		}
	}


	public void onPrivateMessage(PrivateMessageEvent event) {
		String command = event.getMessage();
		String commandLower = command.toLowerCase();
		
		if (commandLower.startsWith("!oracle ")) {
			User user = event.getUser();
			//if (!pm.isAllowed("!oracle",event.getUser(),event.getChannel())) {
			//	event.respond("Sorry, you are not in the access list for consulting the oracle.");
			//	return;
			//}
			scanner = new Scanner(commandLower);
			String token = scanner.next();
			
			if (scanner.hasNext()){
				token = scanner.next();
				
				if (token.equals("list")) {
					if (scanner.hasNext()) {
						token = scanner.next();
						
						if (token.equals("types")) {
							HashMap<Integer,String> hm;
							try {
								hm = Oracle.getTypes(props);
							} catch (SQLException ex) {
								event.respond("Downstream SQL Exception.  Notify maradine.");
								System.out.println(ex);
								ex.printStackTrace();
								return;
							}
							event.getBot().sendMessage(user, "Listing Types:");
							SortedSet<Integer> keys = new TreeSet<Integer>(hm.keySet());
							for (Integer key : keys) {
								String type = hm.get(key);
								event.respond(key+":  "+type);
							}
						
						} else if (token.equals("weapons")) {
							if (scanner.hasNextInt()) {
								int type = scanner.nextInt();
								HashMap<Integer,String> hm;
								try {
									hm = Oracle.getWeapons(props,type);
								} catch (SQLException ex) {
									event.respond("Downstream SQL Exception.  Notify maradine.");
									System.out.println(ex);
									ex.printStackTrace();
									return;
								}
								if (hm.size() > 0) {
									event.getBot().sendMessage(user, "Listing Weapons:");
									SortedSet<Integer> keys = new TreeSet<Integer>(hm.keySet());
									for (Integer key : keys) {
										String weapon = hm.get(key);
										event.respond(key+":  "+weapon);
									}
								} else {
									event.respond("That's not a real type.  Not here.  Not in these parts.");
								}
								//
							} else {
								event.respond("You need a type number. ex. !oracle list weapons 13");
							}
							
						} else if (token.equals("periods")) {
							ArrayList<TimePeriod> al = new ArrayList<TimePeriod>();
							try {
								al = Oracle.getPeriods(props);
							} catch (SQLException ex) {
								event.respond("Downstream SQL Exception.  Notify maradine.");
								System.out.println(ex);
								ex.printStackTrace();
								return;
							}
							event.getBot().sendMessage(user, "Listing Periods:");
							for (TimePeriod tp : al) {
								DateTime dt1 = new DateTime(tp.getStart()*1000L);
								DateTime dt2 = new DateTime(tp.getEnd()*1000L);
								event.respond("id: "+tp.getId()+" start: "+dt1.toString()+" end: "+dt2.toString()+" daily? "+tp.getIsDaily());
							}
							
						}




					} else {
						event.respond("What would you like to list? types, weapons, or periods");
					}
				//END OF 'LIST' HANDLING
				//
				//
				//
				} else if (token.equals("ask")) {
					if (scanner.hasNext("weapon")) {
						scanner.next();
						if (scanner.hasNextInt()) {
							int id = scanner.nextInt();
							HashMap<String,String> hm=new HashMap<String,String>(); 
							try {
								if (scanner.hasNextInt()) {
									int period = scanner.nextInt();
									hm = Oracle.getKillAggregate(props,id,period);
								} else {
									hm = Oracle.getKillAggregate(props,id);
								}
							} catch (SQLException ex) {
								event.respond("Downstream SQL Exception.  Here's the gritty:");
								event.respond(ex.getMessage());
								return;
							}
							if (hm.size()>0) {
								//event.respond("Prepare for Stats:");
								event.respond(hm.get("name")+" -  kills: "+hm.get("kills")+" uniques: "+hm.get("uniques")+" kpu: "+hm.get("kpu")+" avgbr: "+hm.get("avgbr")+" q1kpu: "+hm.get("q1kpu")+" q2kpu: "+hm.get("q2kpu")+" q3kpu: "+hm.get("q3kpu")+" q4kpu: "+hm.get("q4kpu"));


							} else {
								event.respond("Was that a legit weapon id and period?  I didn't get anything back.");
							}
						} else {
							event.respond("Need a weapon id and optionally a period.");
						}
					
					} else if (scanner.hasNext("type")) {
						//blah
					} else {
						event.respond("What, like, just in general? Try !oracle ask <weapon/type> [period]");
					}
				}
				//END OF 'STATS' HANDLING
				//
				//
				//


				else if (token.equals("some other thing")) {
					//blah
				}

				
			}
		}
		
		if (command.equals("!oracle")) {
	
			event.respond("What?");
		}
	}





}


