package pt.isec.pa.chess.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe singleton responsável por gerir os logs do model
 * Permite adicionar, obter e limpar mensagens de log, notificando os observadores sobre alterações
 */
public class ModelLog {
    public static final String PROP_LOGS = "logs";

    private static final ModelLog instance = new ModelLog();
    private final List<String> logs = new ArrayList<>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Construtor privado para implementar o padrão singleton
     */
    private ModelLog() {}

    /**
     * Obtém a instância única da classe ModelLog
     *
     * @return a instância singleton do ModelLog
     */
    public static ModelLog getInstance() {
        return instance;
    }

    /**
     * Adiciona uma nova mensagem de log à lista e notifica os observadores
     *
     * @param log a mensagem de log a adicionar
     */
    public void addLog(String log) {
        logs.add(log);
        pcs.firePropertyChange(PROP_LOGS, null, log);
    }

    /**
     * Obtém uma lista imutável de todas as mensagens de log
     *
     * @return lista não modificável contendo todos os logs
     */
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    /**
     * Remove todas as mensagens de log e notifica os observadores
     */
    public void clearLogs() {
        logs.clear();
        pcs.firePropertyChange(PROP_LOGS, null, null);
    }

    /**
     * Adiciona um observador para ser notificado sobre alterações nos logs
     *
     * @param listener o observador a adicionar
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Remove um observador da lista de notificações
     *
     * @param listener o observador a remover
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}