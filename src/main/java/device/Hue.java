package device;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ai.api.GsonFactory;
import support.Rest;

/**
 * Classe che permette di controllare le luci Philips Hue
 */
public class Hue {

    /**
     * La luminopsita' massima a cui si puo' arrivare
     */
    public static final int MAX_BRIGHTNESS = 254;

    /**
     * Una mappa che ad ogni colore (in lingua ita) assegna il proprio valore in hue
     */
    public static final Map<String, Double[]> COLORS = new HashMap<>();

	/**
	 * L'url in cui si possono trovare le luci
	 */
    private final String lightsURL;

    /**
     * Tutte le luci che sono state registrate dall'url
     */
    private final Map<String, Map<String, Object>> allLights;

    /**
     * L'ultima luminosita' impostata
     */
    private double brightness = 0;

    /**
     * Riempimento della mappa con i vari colori
     */
    static { // todo set right colors (in the simulation they are off, maybe in reality are ok)
        COLORS.put("giall[oae]", new Double[]{0.475, 0.475});
        COLORS.put("ross[oae]", new Double[]{0.7, 0.25});
        COLORS.put("verd[ei]", new Double[]{0.1, 0.55});
        COLORS.put("blu", new Double[]{0.15, 0.175});
        COLORS.put("rosa", new Double[]{0.45, 0.275});
        COLORS.put("viola", new Double[]{0.25, 0.1});
        COLORS.put("azzurr[oae]", new Double[]{0.15, 0.25});
        COLORS.put("arancio(ne|ni)?", new Double[]{0.6, 0.35});
        //COLORS.put("nero", new Double[]{1.0, 1.0});
        COLORS.put("bianc(o|a|he)", new Double[]{0.3, 0.25});
    }

    /**
     * Cerca le luci Philips Hue a ll'indirizzo <a href="http://172.30.1.138/api/C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx/lights/">http://172.30.1.138/api/C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx/lights/</a>
     */
    public Hue () {
        this("172.30.1.138", "C0vPwqjJZo5Jt9Oe5HgO6sBFFMxgoR532IxFoGmx");
    }

    /**
     * Cerca le luci Philips Hue nell'indirizzo specificato e con l'utente specificato.<br>
     * Una volta trovate le luci le setta tutte alla stessa luminosita' e allo stesso colore<br>
     * (per ora fa una media e poi assegna il valore risultante a tutte)
     * @param ip l'indirizzo IP (seguito dalla porta se e' diversa dalla solita 8000)
     * @param user l'utente
     */
    public Hue(String ip, String user) {
        lightsURL = "http://" + ip + "/api/" + user + "/lights/";
        allLights = (Map<String, Map<String, Object>>)Rest.get(lightsURL);

        if(allLights.size() != 0) {
            double bri = 0;
            double hue = 0;
            for (String name: allLights.keySet()) {
                Map<String, Object> state = (Map<String, Object>)allLights.get(name).get("state");
                bri += (Double) state.get("bri");
                hue += (Double) state.get("hue");
            }
            bri = bri/allLights.size();
            hue = hue/allLights.size();
            setState("bri", String.valueOf(bri));
            setState("hue", String.valueOf(hue));

            brightness = (bri*MAX_BRIGHTNESS)/100;
        }
    }

    /**
     * Ritorna un insieme contenente tutti i nomi delle luci che si sono trovate
     *
     * @return l'insieme dei nomi delle luci
     */
    public Set<String> getNameLights() { return allLights.keySet(); }

    /**
     * Rimuove dal controllo tutte le luci che hanno il nome uguale ad uno contenuto nell'insieme passato
     * 
     * @param toRemove le luci da rimuovere
     */
    public void removeLights(Set<String> toRemove) {
        for(String string : toRemove)
            allLights.remove(string);
    }

    /**
     * Accende tutte le luci controllate
     */
    public void turnOn() { setState("on", "true"); }

    /**
     * Spegne tutte le luci controllate
     */
    public void turnOff() { setState("on", "false"); }
    
    /**
     * Ritorna la liminosita' attuale delle luci controllate
     * @return
     */
    public double getCurrentBrightness() { return brightness; }
    
    /**
     * Modifica la luminosita' delle luci a seconda del valore inserito
     * @param num la luminosita' che si vuole da (0 a 100)
     */
    public void setBrightness(double num) {
    	if (num<0)
    		num=0;
    	else if (num>100)
    	    num=100;

    	setState("bri", String.valueOf( (num*MAX_BRIGHTNESS)/100) );
        brightness = num;
    }

    /**
     * Aumenta la luminosita' delle luci controllate della percentuale che viene passata
     * @param percentage la percentuale di aumento della luminosita'
     */
    public void increaseBrightness(double percentage) {
        if (percentage<0)
            percentage = 0;
        else if (percentage>100)
            percentage = 100;
        setBrightness(brightness + percentage);
    }

    /**
     * Aumenta la luminosita' delle luci controllate del 15%
     */
    public void increaseBrightness() { increaseBrightness(15); }

    /**
     * Dinuisce la luminosita' delle luci controllate della percentuale che viene passata
     * @param percentage la percentuale di diminuzione della luminosita'
     */
    public void decreaseBrightness(int percentage) {
        if (percentage<0)
            percentage = 0;
        else if (percentage>100)
            percentage = 100;
        setBrightness(brightness - percentage);
    }

    /**
     * Dinuisce la luminosita' delle luci controllate del 15%
     */
    public void decreaseBrightness() { decreaseBrightness(15); }

    public void changeColor(String colorName) {
        for (String regex: COLORS.keySet())
            if(colorName.matches("(?i)" + regex))
                setState("xy", GsonFactory.getDefaultFactory().getGson().toJson(COLORS.get(regex)));
    }

    /**
     * Modifica il colore delle luci in modo da fare un bel effetto arcobaleno continuo
     */
    public void colorLoop() { setState("effect", "colorloop"); }

    /**
     * Funzione generale per poter utilizzare qualunque valore, ma non funziona<br>
     * Da testare, visto che mi sembra strano che non funzi...<br>
     * e invece funziona.
     */
    public void setState(String attribute, String value){
        for (String light : allLights.keySet()) {
            Map<String, Object> state = (Map<String, Object>)allLights.get(light).get("state");
            Rest.put(lightsURL + light + "/state",
                    "{ \"" + attribute + "\" : " + value + ", \"transitiontime\": 10 }", // todo check transition
                    "application/json");
            state.put(attribute, value);
        }
    }
}
