/*
 * Copyright (c) 2013 IRCCloud, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.irccloud.android;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.irccloud.android.data.EventsDataSource;
import com.irccloud.android.data.ServersDataSource;

public class CollapsedEventsList {
    public static final int TYPE_NETSPLIT = -1;
	public static final int TYPE_JOIN = 0;
	public static final int TYPE_PART = 1;
	public static final int TYPE_QUIT = 2;
	public static final int TYPE_MODE = 3;
	public static final int TYPE_POPIN = 4;
	public static final int TYPE_POPOUT = 5;
	public static final int TYPE_NICKCHANGE = 6;
    public static final int TYPE_CONNECTIONSTATUS = 7;

	public static final int MODE_OWNER = 0;
	public static final int MODE_ADMIN = 1;
	public static final int MODE_OP = 2;
	public static final int MODE_HALFOP = 3;
	public static final int MODE_VOICE = 4;

    public static final int MODE_DEOWNER = 5;
    public static final int MODE_DEADMIN = 6;
    public static final int MODE_DEOP = 7;
    public static final int MODE_DEHALFOP = 8;
    public static final int MODE_DEVOICE = 9;

    public static final int MODE_COUNT = 10;

    public boolean showChan = false;

	public class CollapsedEvent {
        long eid;
		int type;
        boolean modes[] = new boolean[MODE_COUNT];
		String nick;
		String old_nick;
		String hostmask;
		String msg;
		String from_mode;
        String from_nick;
		String target_mode;
        String chan;
        boolean netsplit;
        int count;
		
		public String toString() {
			return "{type: " + type + ", nick: " + nick + ", old_nick: " + old_nick + ", hostmask: " + hostmask + ", msg: " + msg + "netsplit: " + netsplit + "}";
		}

        public int modeCount() {
            int count = 0;
            for(int i = 0; i < MODE_COUNT; i++) {
                if(modes[i])
                    count++;
            }
            return count;
        }

        public boolean addMode(String mode) {
            if(mode.equalsIgnoreCase(server!=null?server.MODE_OWNER:"q")) {
                if(modes[MODE_DEOWNER])
                    modes[MODE_DEOWNER] = false;
                else
                    modes[MODE_OWNER] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_ADMIN:"a")) {
                if(modes[MODE_DEADMIN])
                    modes[MODE_DEADMIN] = false;
                else
                    modes[MODE_ADMIN] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_OP:"o")) {
                if(modes[MODE_DEOP])
                    modes[MODE_DEOP] = false;
                else
                    modes[MODE_OP] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_HALFOP:"h")) {
                if(modes[MODE_DEHALFOP])
                    modes[MODE_DEHALFOP] = false;
                else
                    modes[MODE_HALFOP] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_VOICED:"v")) {
                if(modes[MODE_DEVOICE])
                    modes[MODE_DEVOICE] = false;
                else
                    modes[MODE_VOICE] = true;
            } else {
                return false;
            }
            if(modeCount() == 0)
                return addMode(mode);
            return true;
        }

        public boolean removeMode(String mode) {
            if(mode.equalsIgnoreCase(server!=null?server.MODE_OWNER:"q")) {
                if(modes[MODE_OWNER])
                    modes[MODE_OWNER] = false;
                else
                    modes[MODE_DEOWNER] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_ADMIN:"a")) {
                if(modes[MODE_ADMIN])
                    modes[MODE_ADMIN] = false;
                else
                    modes[MODE_DEADMIN] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_OP:"o")) {
                if(modes[MODE_OP])
                    modes[MODE_OP] = false;
                else
                    modes[MODE_DEOP] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_HALFOP:"h")) {
                if(modes[MODE_HALFOP])
                    modes[MODE_HALFOP] = false;
                else
                    modes[MODE_DEHALFOP] = true;
            } else if(mode.equalsIgnoreCase(server!=null?server.MODE_VOICED:"v")) {
                if(modes[MODE_VOICE])
                    modes[MODE_VOICE] = false;
                else
                    modes[MODE_DEVOICE] = true;
            } else {
                return false;
            }
            if(modeCount() == 0)
                return addMode(mode);
            return true;
        }

        public String getModes(boolean showSymbol) {
            final String[] mode_msgs = {
                "promoted to owner",
                "promoted to admin",
                "opped",
                "halfopped",
                "voiced",
                "demoted from owner",
                "demoted from admin",
                "de-opped",
                "de-halfopped",
                "de-voiced",
            };

            String output = null;
            if(modeCount() > 0) {
                output = "";

                for(int i = 0; i < MODE_COUNT; i++) {
                    if(modes[i]) {
                        if(output.length() > 0)
                            output += ", ";
                        output += mode_msgs[i];
                        if(showSymbol) {
                            output += " (\u0004" + mode_colors.get(mode_modes[i].substring(1)) + mode_modes[i] + "\u000f)";
                        }
                    }
                }
            }

            return output;
        }
    }
	
	public class comparator implements Comparator<CollapsedEvent> {
		public int compare(CollapsedEvent e1, CollapsedEvent e2) {
			if(e1.type == e2.type) {
                if(e1.eid > e2.eid)
    				return 1;
                else
                    return -1;
            } else if(e1.type > e2.type) {
				return 1;
            } else {
				return -1;
            }
		}
	}
	
	private ArrayList<CollapsedEvent> data = new ArrayList<CollapsedEvent>();
    private ServersDataSource.Server server;
    private HashMap<String, String> mode_colors;
    private String mode_modes[];

    public CollapsedEventsList() {
        setServer(null);
    }

    public void setServer(ServersDataSource.Server s) {
        server = s;
        if(server != null) {
            mode_colors = new HashMap<String,String>() {{
                put(server.MODE_OWNER,"E7AA00");
                put(server.MODE_ADMIN,"6500A5");
                put(server.MODE_OP,"BA1719");
                put(server.MODE_HALFOP,"B55900");
                put(server.MODE_VOICED,"25B100");
            }};
            mode_modes = new String[] {
                    "+" + server.MODE_OWNER,
                    "+" + server.MODE_ADMIN,
                    "+" + server.MODE_OP,
                    "+" + server.MODE_HALFOP,
                    "+" + server.MODE_VOICED,
                    "-" + server.MODE_OWNER,
                    "-" + server.MODE_ADMIN,
                    "-" + server.MODE_OP,
                    "-" + server.MODE_HALFOP,
                    "-" + server.MODE_VOICED
            };
        } else {
            mode_colors = new HashMap<String,String>() {{
                put("q","E7AA00");
                put("a","6500A5");
                put("o","BA1719");
                put("h","B55900");
                put("v","25B100");
            }};
            mode_modes = new String[] {
                    "+q", "+a", "+o", "+h", "+v", "-q", "-a", "-o", "-h", "-v"
            };
        }
    }

    public String toString() {
        String out = "CollapsedEventsList {\n";
        for(int i = 0; i < data.size(); i++) {
            out += "\t" + data.get(i).toString() + "\n";
        }
        out += "}";
        return out;
    }

    public boolean addEvent(EventsDataSource.Event event) {
        String type = event.type;
        if(type.startsWith("you_"))
            type = type.substring(4);

        if(type.equalsIgnoreCase("joined_channel")) {
            addEvent(event.eid, CollapsedEventsList.TYPE_JOIN, event.nick, null, event.hostmask, event.from_mode, null, event.chan);
        } else if(type.equalsIgnoreCase("parted_channel")) {
            addEvent(event.eid, CollapsedEventsList.TYPE_PART, event.nick, null, event.hostmask, event.from_mode, event.msg, event.chan);
        } else if(type.equalsIgnoreCase("quit")) {
            addEvent(event.eid, CollapsedEventsList.TYPE_QUIT, event.nick, null, event.hostmask, event.from_mode, event.msg, event.chan);
        } else if(type.equalsIgnoreCase("nickchange")) {
            addEvent(event.eid, CollapsedEventsList.TYPE_NICKCHANGE, event.nick, event.old_nick, null, event.from_mode, null, event.chan);
        } else if(type.equalsIgnoreCase("socket_closed") || type.equalsIgnoreCase("connecting_failed") || type.equalsIgnoreCase("connecting_cancelled")) {
            addEvent(event.eid, CollapsedEventsList.TYPE_CONNECTIONSTATUS, null, null, null, null, event.msg, null);
        } else if(type.equalsIgnoreCase("user_channel_mode")) {
            JsonNode ops = event.ops;
            if(ops != null) {
                CollapsedEvent e = findEvent(event.nick, event.chan);
                if(e == null) {
                    e = new CollapsedEvent();
                    e.type = TYPE_MODE;
                    e.hostmask = event.hostmask;
                    e.target_mode = event.target_mode;
                    e.nick = event.nick;
                    e.chan = event.chan;
                }
                JsonNode add = ops.get("add");
                for(int i = 0; i < add.size(); i++) {
                    JsonNode op = add.get(i);
                    if(!e.addMode(op.get("mode").asText()))
                        return false;
                    if(e.type == TYPE_MODE) {
                        if(event.from != null && event.from.length() > 0) {
                            e.from_nick = event.from;
                            e.from_mode = event.from_mode;
                        } else {
                            e.from_nick = event.server;
                            e.from_mode = "__the_server__";
                        }
                    } else {
                        e.from_mode = event.target_mode;
                    }
                }
                JsonNode remove = ops.get("remove");
                for(int i = 0; i < remove.size(); i++) {
                    JsonNode op = remove.get(i);
                    if(!e.removeMode(op.get("mode").asText()))
                        return false;
                    if(e.type == TYPE_MODE) {
                        if(event.from != null && event.from.length() > 0) {
                            e.from_nick = event.from;
                            e.from_mode = event.from_mode;
                        } else {
                            e.from_nick = event.server;
                            e.from_mode = "__the_server__";
                        }
                    } else {
                        e.from_mode = event.target_mode;
                    }
                }
                if(!data.contains(e))
                    data.add(e);
            }
        }
        return true;
    }

	public void addEvent(long eid, int type, String nick, String old_nick, String hostmask, String from_mode, String msg, String chan) {
		addEvent(eid, type, nick, old_nick, hostmask, from_mode, msg, null, chan);
	}
	
	public void addEvent(long eid, int type, String nick, String old_nick, String hostmask, String from_mode, String msg, String target_mode, String chan) {
		CollapsedEvent e = null;
		
		if(type < TYPE_NICKCHANGE) {
            if(showChan) {
                if(type == TYPE_QUIT) {
                    boolean found = false;
                    for(CollapsedEvent ev : data) {
                        if(ev.type == TYPE_JOIN) {
                            ev.type = TYPE_POPIN;
                            found = true;
                        }
                    }
                    if(found)
                        return;
                } else if(type == TYPE_JOIN) {
                    for(CollapsedEvent ev : data) {
                        if(ev.type == TYPE_QUIT) {
                            ev.type = TYPE_POPOUT;
                            return;
                        }
                    }
                }
            }

			if(old_nick != null && type != TYPE_MODE) {
				e = findEvent(old_nick, chan);
				if(e != null)
					e.nick = nick;
			}
			
			if(e == null)
				e = findEvent(nick, chan);
			
			if(e == null) {
				e = new CollapsedEvent();
                e.eid = eid;
				e.type = type;
				e.nick = nick;
				e.old_nick = old_nick;
				e.hostmask = hostmask;
				e.from_mode = from_mode;
				e.msg = msg;
				e.target_mode = target_mode;
                e.chan = chan;
				data.add(e);
			} else {
                e.eid = eid;
				if(e.type == TYPE_MODE) {
					e.type = type;
					e.msg = msg;
					e.old_nick = old_nick;
                    e.hostmask = hostmask;
					if(from_mode != null)
						e.from_mode = from_mode;
					if(target_mode != null)
						e.target_mode = target_mode;
                } else if(type != e.type && e.type == TYPE_NICKCHANGE) {
                    e.type = type;
                    e.from_mode = from_mode;
                    e.hostmask = hostmask;
                    e.nick = nick;
                    e.msg = msg;
				} else if(type == TYPE_MODE) {
					e.from_mode = target_mode;
				} else if(e.type == type) {
				} else if(type == TYPE_JOIN) {
                    if(e.type == TYPE_POPIN)
                        e.type = TYPE_JOIN;
                    else
    					e.type = TYPE_POPOUT;
					e.from_mode = from_mode;
                    e.chan = chan;
				} else if(e.type == TYPE_POPOUT) {
					e.type = type;
				} else {
					e.type = TYPE_POPIN;
				}
			}
		} else {
			if(type == TYPE_NICKCHANGE) {
                for (CollapsedEvent e1 : data) {
                    if (e1.type == TYPE_NICKCHANGE && e1.nick.equalsIgnoreCase(old_nick)) {
                        if (e1.old_nick.equalsIgnoreCase(nick))
                            data.remove(e1);
                        else
                            e1.nick = nick;
                        return;
                    }
                    if ((e1.type == TYPE_JOIN || e1.type == TYPE_POPOUT) && e1.nick.equalsIgnoreCase(old_nick)) {
                        e1.old_nick = old_nick;
                        e1.nick = nick;
                        for (CollapsedEvent e2 : data) {
                            if ((e2.type == TYPE_QUIT || e2.type == TYPE_PART) && e2.nick.equalsIgnoreCase(nick)) {
                                e1.type = TYPE_POPOUT;
                                data.remove(e2);
                                break;
                            }
                        }
                        if(data.size() > 0)
                            return;
                    }
                    if ((e1.type == TYPE_QUIT || e1.type == TYPE_PART) && e1.nick.equalsIgnoreCase(nick)) {
                        e1.type = TYPE_POPOUT;
                        for (CollapsedEvent e2 : data) {
                            if (e2.type == TYPE_JOIN && e2.nick.equalsIgnoreCase(old_nick)) {
                                data.remove(e2);
                                break;
                            }
                        }
                        if(data.size() > 0)
                            return;
                    }
                }
                e = new CollapsedEvent();
                e.eid = eid;
                e.type = type;
                e.nick = nick;
                e.from_mode = from_mode;
                e.old_nick = old_nick;
                e.hostmask = hostmask;
                e.msg = msg;
                e.chan = chan;
                data.add(e);
            } else if(type == TYPE_CONNECTIONSTATUS) {
                for (CollapsedEvent e1 : data) {
                    if(e1.msg.equals(msg)) {
                        e1.count++;
                        return;
                    }
                }
                e = new CollapsedEvent();
                e.eid = eid;
                e.type = type;
                e.msg = msg;
                e.count = 1;
                data.add(e);
            } else {
				e = new CollapsedEvent();
                e.eid = eid;
				e.type = type;
				e.nick = nick;
                e.from_mode = from_mode;
				e.old_nick = old_nick;
				e.hostmask = hostmask;
				e.msg = msg;
                e.chan = chan;
				data.add(e);
			}
		}

        if(e != null && type == TYPE_QUIT && msg != null) {
            if(msg.matches("(?:[^\\s:\\/.]+\\.)+[a-z]{2,} (?:[^\\s:\\/.]+\\.)+[a-z]{2,}")) {
                String[] parts = msg.split(" ");
                if(parts.length > 1 && !parts[0].equals(parts[1])) {
                    e.netsplit = true;
                    boolean found = false;
                    for(CollapsedEvent c : data) {
                        if(c.type == TYPE_NETSPLIT && c.msg.equalsIgnoreCase(msg))
                            found = true;
                    }
                    if(!found && data.size() > 1) {
                        CollapsedEvent c = new CollapsedEvent();
                        c.eid = eid;
                        c.type = TYPE_NETSPLIT;
                        c.msg = msg;
                        data.add(c);
                    }
                }
            }
        }
	}
	
	public CollapsedEvent findEvent(String nick, String chan) {
		for(CollapsedEvent e : data) {
			if(e.nick != null && (e.nick.equalsIgnoreCase(nick) && (e.chan == null || e.chan.equalsIgnoreCase(chan))))
				return e;
		}
		return null;
	}
	
	public String formatNick(String nick, String from_mode, boolean colorize) {
        ObjectNode PREFIX = null;
        if(server != null)
            PREFIX = server.PREFIX;

        if(PREFIX == null) {
            PREFIX = new ObjectMapper().createObjectNode();
            PREFIX.put(server != null ? server.MODE_OWNER : "q", "~");
            PREFIX.put(server!=null?server.MODE_ADMIN:"a", "&");
            PREFIX.put(server!=null?server.MODE_OP:"o", "@");
            PREFIX.put(server!=null?server.MODE_HALFOP:"h", "%");
            PREFIX.put(server!=null?server.MODE_VOICED:"v", "+");
        }

        String[] colors = {"fc009a", "ff1f1a", "d20004", "fd6508", "880019", "c7009c", "804fc4", "5200b7", "123e92", "1d40ff", "108374", "2e980d", "207607", "196d61"};
        String color = null;

        if(colorize) {
            // Normalise a bit
            // typically ` and _ are used on the end alone
            String normalizedNick = nick.toLowerCase().replaceAll("[`_]+$","");
            //remove |<anything> from the end
            normalizedNick = normalizedNick.replaceAll("|.*$","");

            Double hash = 0.0;

            for(int i = 0; i < normalizedNick.length(); i++) {
                hash = ((int)normalizedNick.charAt(i)) + (double)((int)(hash.longValue()) << 6) + (double)((int)(hash.longValue()) << 16) - hash;
            }

            color = colors[(int)Math.abs(hash.longValue() % 14)];
        }

		StringBuilder output = new StringBuilder();
		boolean showSymbol = false;
		try {
			if(NetworkConnection.getInstance().getUserInfo() != null && NetworkConnection.getInstance().getUserInfo().prefs != null)
			    showSymbol = NetworkConnection.getInstance().getUserInfo().prefs.getBoolean("mode-showsymbol");
		} catch (Exception e) {
		}
		String mode = "";
		if(from_mode != null && from_mode.length() > 0) {
            if(from_mode.contains(server!=null?server.MODE_OWNER:"q"))
                mode = server!=null?server.MODE_OWNER:"q";
            else if(from_mode.contains(server!=null?server.MODE_ADMIN:"a"))
                mode = server!=null?server.MODE_ADMIN:"a";
            else if(from_mode.contains(server!=null?server.MODE_OP:"o"))
                mode = server!=null?server.MODE_OP:"o";
            else if(from_mode.contains(server!=null?server.MODE_HALFOP:"h"))
                mode = server!=null?server.MODE_HALFOP:"h";
            else if(from_mode.contains(server!=null?server.MODE_VOICED:"v"))
                mode = server!=null?server.MODE_VOICED:"v";
            else
    			mode = from_mode.substring(0,1);
		}
		if(mode != null && mode.length() > 0) {
            if(mode_colors.containsKey(mode))
                output.append("\u0004" + mode_colors.get(mode) + "\u0002");
            else
                output.append("\u0002");
			if(showSymbol) {
                if(PREFIX.has(mode))
                    output.append(TextUtils.htmlEncode(PREFIX.get(mode).asText()));
			} else {
                output.append("•");
			}
            output.append("\u000f ");
		}

        if(color != null)
            output.append("\u0004" + color);
		output.append(nick);
        if(color != null)
            output.append("\u0004");
		return output.toString();
	}
	
	private String was(CollapsedEvent e) {
		StringBuilder was = new StringBuilder();
		String modes = e.getModes(false);

		if(e.old_nick != null && e.type != TYPE_MODE)
			was.append("was ").append(e.old_nick);
		if(modes != null && modes.length() > 0) {
			if(was.length() > 0)
				was.append("; ");
            was.append("\u00031"+modes+"\u000f");
		}
		
		if(was.length() > 0)
			was.insert(0, " (").append(")");
		
		return was.toString();
	}
	
	public String getCollapsedMessage() {
		StringBuilder message = new StringBuilder();

		if(data.size() == 0)
			return null;
		
		if(data.size() == 1 && data.get(0).modeCount() < ((data.get(0).type == TYPE_MODE)?2:1)) {
			CollapsedEvent e = data.get(0);
			switch(e.type) {
            case TYPE_NETSPLIT:
                message.append(e.msg.replace(" ", " ↮ "));
                break;
			case TYPE_MODE:
				message.append("<b>").append(formatNick(e.nick, e.target_mode, false)).append("</b> was " + e.getModes(true));
				if(e.from_nick != null) {
                    if(e.from_mode != null && e.from_mode.equalsIgnoreCase("__the_server__"))
    					message.append(" by the server <b>").append(e.from_nick).append("</b>");
                    else
                        message.append(" by ").append(formatNick(e.from_nick, e.from_mode, false));
                }
				break;
			case TYPE_JOIN:
	    		message.append("→ <b>").append(formatNick(e.nick, e.from_mode, false)).append("</b>").append(was(e));
	    		message.append(" joined");
                if(showChan)
                    message.append(" " + e.chan);
                message.append(" (").append(e.hostmask + ")");
				break;
			case TYPE_PART:
	    		message.append("← <b>").append(formatNick(e.nick, e.from_mode, false)).append("</b>").append(was(e));
	    		message.append(" left");
                if(showChan)
                    message.append(" " + e.chan);
                message.append(" (").append(e.hostmask + ")");
	    		if(e.msg != null && e.msg.length() > 0)
	    			message.append(": ").append(e.msg);
				break;
			case TYPE_QUIT:
	    		message.append("⇐ <b>").append(formatNick(e.nick, e.from_mode, false)).append("</b>").append(was(e));
	    		if(e.hostmask != null)
	    			message.append(" quit (").append(e.hostmask).append(") ");
	    		else
	    			message.append(" quit: ");
                if(e.msg != null && e.msg.length() > 0)
                    message.append(e.msg);
				break;
			case TYPE_NICKCHANGE:
	    		message.append(e.old_nick).append(" → <b>").append(formatNick(e.nick, e.from_mode, false)).append("</b>");
				break;
			case TYPE_POPIN:
	    		message.append("↔ <b>").append(formatNick(e.nick, e.from_mode, false)).append("</b>").append(was(e));
	    		message.append(" popped in");
                if(showChan)
                    message.append(" " + e.chan);
	    		break;
			case TYPE_POPOUT:
	    		message.append("↔ <b>").append(formatNick(e.nick, e.from_mode, false)).append("</b>").append(was(e));
	    		message.append(" nipped out");
                if(showChan)
                    message.append(" " + e.chan);
	    		break;
            case TYPE_CONNECTIONSTATUS:
                message.append(e.msg);
                if(e.count > 1)
                    message.append(" (" + e.count + "x)");
                break;
			}
		} else {
            boolean netsplit = false;
			Collections.sort(data, new comparator());
			Iterator<CollapsedEvent> i = data.iterator();
			CollapsedEvent last = null;
			CollapsedEvent next = i.next();
			CollapsedEvent e;
			int groupcount = 0;
			
			while(next != null) {
				e = next;

                do {
                    if(i.hasNext())
                        next = i.next();
                    else
                        next = null;
                } while(next != null && netsplit && next.netsplit);
				
				if(message.length() > 0 && e.type < TYPE_NICKCHANGE && ((next == null || next.type != e.type) && last != null && last.type == e.type)) {
					if(groupcount == 1)
						message.delete(message.length() - 2, message.length()).append(" ");
					message.append("and ");
				}
				
				if(last == null || last.type != e.type) {
					switch(e.type) {
                    case TYPE_NETSPLIT:
                        netsplit = true;
                        break;
					case TYPE_MODE:
						if(message.length() > 0)
							message.append("• ");
						message.append("\u00031mode:\u000f ");
						break;
					case TYPE_JOIN:
						message.append("→ ");
						break;
					case TYPE_PART:
						message.append("← ");
						break;
					case TYPE_QUIT:
						message.append("⇐ ");
						break;
					case TYPE_NICKCHANGE:
						if(message.length() > 0)
							message.append("• ");
						break;
					case TYPE_POPIN:
					case TYPE_POPOUT:
						message.append("↔ ");
						break;
                    case TYPE_CONNECTIONSTATUS:
                        break;
					}
				}

				if(e.type == TYPE_NICKCHANGE) {
					message.append(e.old_nick).append(" → <b>").append(formatNick(e.nick, e.from_mode, false)).append("</b>");
					String old_nick = e.old_nick;
					e.old_nick = null;
					message.append(was(e));
					e.old_nick = old_nick;
                } else if(e.type == TYPE_NETSPLIT) {
                    message.append(e.msg.replace(" ", " ↮ "));
                } else if(e.type == TYPE_CONNECTIONSTATUS) {
                    message.append(e.msg);
                    if(e.count > 1)
                        message.append(" (" + e.count + "x)");
				} else if(!showChan) {
					message.append("<b>").append(formatNick(e.nick, (e.type == TYPE_MODE)?e.target_mode:e.from_mode, false)).append("</b>").append(was(e));
				}
				
				if((next == null || next.type != e.type) && !showChan) {
					switch(e.type) {
					case TYPE_JOIN:
						message.append(" joined");
						break;
					case TYPE_PART:
						message.append(" left");
						break;
					case TYPE_QUIT:
						message.append(" quit");
						break;
					case TYPE_POPIN:
						message.append(" popped in");
						break;
					case TYPE_POPOUT:
						message.append(" nipped out");
						break;
					}
				} else if(showChan && e.type != TYPE_NETSPLIT && e.type != TYPE_CONNECTIONSTATUS) {
                    if(groupcount == 0) {
                        message.append("<b>").append(formatNick(e.nick, (e.type == TYPE_MODE)?e.target_mode:e.from_mode, false)).append("</b>").append(was(e));
                        switch(e.type) {
                            case TYPE_JOIN:
                                message.append(" joined ");
                                break;
                            case TYPE_PART:
                                message.append(" left ");
                                break;
                            case TYPE_QUIT:
                                message.append(" quit ");
                                break;
                            case TYPE_POPIN:
                                message.append(" popped in ");
                                break;
                            case TYPE_POPOUT:
                                message.append(" nipped out ");
                                break;
                        }
                    }
                    if(e.type != TYPE_QUIT)
                        message.append(e.chan);
                }

				if(next != null && next.type == e.type) {
                    if(message.length() > 0) {
                        message.append(", ");
                        groupcount++;
                    }
				} else if(next != null) {
					message.append(" ");
					groupcount = 0;
				}
				
				last = e;
			}
		}
		return message.toString();
	}
	
	public void clear() {
		data.clear();
	}

    public int size() {
        return data.size();
    }
}
