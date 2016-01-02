/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FatRatApplet.java
 *
 * Created on 10.9.2011, 22:05:53
 */
package info.dolezel.fatrat.applet;

import info.dolezel.fatrat.applet.components.SpeedGraph;
import info.dolezel.fatrat.applet.data.TransferClass;
import info.dolezel.fatrat.applet.dialogs.NewTransfer;
import info.dolezel.fatrat.applet.models.QueueModel;
import info.dolezel.fatrat.applet.models.TransferModel;
import info.dolezel.fatrat.applet.models.data.NameAndState;
import info.dolezel.fatrat.applet.models.renderers.NameAndStateRenderer;
import info.dolezel.fatrat.applet.models.renderers.ProgressRenderer;
import info.dolezel.fatrat.applet.models.renderers.SpeedIconRenderer;
import info.dolezel.fatrat.applet.settings.AppletSettings;
import info.dolezel.fatrat.applet.util.TrustAllCertificates;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author lubos
 */
public class FatRatApplet extends javax.swing.JApplet implements IconLoader {
    static public final int[] refreshIntervals = {2, 5, 15, 30};
    
    boolean runAsStandalone = false;
    URL standaloneVirtualURL;
    int lastTabIndex = 0;
    
    XmlRpcClient client;
    Timer timer;
    QueueModel queueModel;
    TransferModel transferModel;
    String baseURL;
    TransferClass[] transferClasses;
    
    SpeedGraph graphTransfer, graphQueue;
    String lastGlobalLog, lastTransferLog;
    
    AppletSettings settings;
    TransferPopupMenu transferPopupMenu;
    
    public Action actionResume, actionForceResume, actionPause;
    public Action actionMoveToTop, actionMoveUp, actionMoveDown, actionMoveToBottom;
    public Action actionRemove, actionRemoveWithData;

    /** Initializes the applet FatRatApplet */
    @Override
    public void init() {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
        }
        //</editor-fold>

        /* Create and display the applet */
        try {
            TrustAllCertificates.setup();
            settings = new AppletSettings(getAppletContext());
            
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    initComponents();
                    
                    setupToolbarIcons();
                    setupStateIcons();
                    
                    queueModel = new QueueModel();
                    queues.setModel(queueModel);
                    
                    transferModel = new TransferModel();
                    transfers.setModel(transferModel);
                    
                    TableColumnModel m = transfers.getColumnModel();
                    
                    m.getColumn(1).setCellRenderer(new ProgressRenderer());
                    m.getColumn(0).setCellRenderer(new NameAndStateRenderer());
                    
                    TableColumn col = m.getColumn(0);
                    col.setPreferredWidth(250);
                    
                    col = m.getColumn(3);
                    col.setHeaderRenderer(new SpeedIconRenderer(loadIcon("/css/icons/download.png")));
                    col = m.getColumn(4);
                    col.setHeaderRenderer(new SpeedIconRenderer(loadIcon("/css/icons/upload.png")));
                    
                    graphTransfer = new SpeedGraph();
                    graphQueue = new SpeedGraph();
                    graphTransfer.setName("Transfer Speed Graph");
                    graphQueue.setName("Queue Speed Graph");
                    mainTab.add(graphTransfer, 2);
                    mainTab.add(graphQueue, 3);
                    
                    transfers.getSelectionModel().addListSelectionListener(new TransferSelectionListener());
                }

                private void setupToolbarIcons() {
                    actionAdd.setIcon(loadIcon("/css/icons/add.png"));
                    buttonDelete.setIcon(loadIcon("/css/icons/delete.png"));
                    buttonDeleteWithData.setIcon(loadIcon("/css/icons/delete_with_data.png"));
                    buttonRemoveCompleted.setIcon(loadIcon("/css/icons/states/completed.png"));
                    //buttonResume.setIcon(loadIcon("/css/icons/states/active.png"));
                    //buttonForceResume.setIcon(loadIcon("/css/icons/states/forcedactive.png"));
                    //buttonPause.setIcon(loadIcon("/css/icons/states/paused.png"));
                    //buttonMoveToTop.setIcon(loadIcon("/css/icons/move/top.png"));
                    //buttonMoveUp.setIcon(loadIcon("/css/icons/move/up.png"));
                    //buttonMoveDown.setIcon(loadIcon("/css/icons/move/down.png"));
                    //buttonMoveToBottom.setIcon(loadIcon("/css/icons/move/bottom.png"));
                    
                    actionResume = new StateAction("Resume", loadIcon("/css/icons/states/active.png"));
                    actionPause = new StateAction("Pause", loadIcon("/css/icons/states/paused.png"));
                    actionForceResume = new StateAction("Force resume", loadIcon("/css/icons/states/forcedactive.png"));
                    
                    buttonResume.setAction(actionResume);
                    buttonResume.setActionCommand("Active");
                    buttonForceResume.setActionCommand("ForcedActive");
                    buttonForceResume.setAction(actionForceResume);
                    buttonPause.setActionCommand("Pause");
                    buttonPause.setAction(actionPause);
                    
                    actionMoveToTop = new MoveAction("Move to top", loadIcon("/css/icons/move/top.png"));
                    actionMoveUp = new MoveAction("Move up", loadIcon("/css/icons/move/up.png"));
                    actionMoveDown = new MoveAction("Move down", loadIcon("/css/icons/move/down.png"));
                    actionMoveToBottom = new MoveAction("Move to bottom", loadIcon("/css/icons/move/bottom.png"));
                    
                    buttonMoveToTop.setAction(actionMoveToTop);
                    buttonMoveToTop.setActionCommand("top");
                    buttonMoveUp.setAction(actionMoveUp);
                    buttonMoveUp.setActionCommand("up");
                    buttonMoveDown.setAction(actionMoveDown);
                    buttonMoveDown.setActionCommand("down");
                    buttonMoveToBottom.setAction(actionMoveToBottom);
                    buttonMoveToBottom.setActionCommand("bottom");
                    
                    actionRemove = actionRemoveWithData = new AbstractAction() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            removeTransfers("data".equals(e.getActionCommand()));
                        }
                        
                    };
                    
                    buttonDeleteWithData.setActionCommand("data");
                    
                    transferPopupMenu = new TransferPopupMenu(FatRatApplet.this);
                }

                private void setupStateIcons() {
                    NameAndState.addIcon("Active", loadIcon("/css/icons/states/active.png"));
                    NameAndState.addIcon("Active_upload", loadIcon("/css/icons/states/active_upload.png"));
                    NameAndState.addIcon("Completed", loadIcon("/css/icons/states/completed.png"));
                    NameAndState.addIcon("Completed_upload", loadIcon("/css/icons/states/completed_upload.png"));
                    NameAndState.addIcon("Failed", loadIcon("/css/icons/states/failed_upload.png"));
                    NameAndState.addIcon("Failed_upload", loadIcon("/css/icons/states/failed_upload.png"));
                    NameAndState.addIcon("ForcedActive", loadIcon("/css/icons/states/forcedactive.png"));
                    NameAndState.addIcon("ForcedActive_upload", loadIcon("/css/icons/states/forcedactive_upload.png"));
                    NameAndState.addIcon("Paused", loadIcon("/css/icons/states/paused.png"));
                    NameAndState.addIcon("Paused_upload", loadIcon("/css/icons/states/paused_upload.png"));
                    NameAndState.addIcon("Waiting", loadIcon("/css/icons/states/waiting.png"));
                    NameAndState.addIcon("Waiting_upload", loadIcon("/css/icons/states/waiting_upload.png"));
                }

            });
            setupXmlRpc();
            loadTransferClasses();
            
            // Setup UI updating
            ActionListener timerListener = new RegularUpdateListener();
            int ix = settings.getValue("refreshInterval", 1);
            int secs = refreshIntervals[ix];
            
            timer = new Timer(secs*1000, timerListener);
            timer.setInitialDelay(0);
            timer.start();
            
            refreshInterval.setSelectedIndex(ix);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupXmlRpc() throws MalformedURLException {
        // Initialize XML-RPC
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        URL base = this.getDocumentBase();
        String userInfo = base.getUserInfo();
        StringBuilder sb = new StringBuilder();
        
        sb.append(base.getProtocol()).append("://");
        
        if (userInfo != null)
            sb.append(userInfo).append('@');
        
        sb.append(base.getHost()).append(":").append(base.getPort());
        baseURL = sb.toString();
        
        sb.append("/xmlrpc");
        URL url = new URL(sb.toString());
        
        System.out.println("XML-RPC URL is: "+url);
        
        config.setServerURL(url);
        config.setEnabledForExtensions(true);
        
        if (userInfo != null) {
            System.out.println(userInfo);
            String[] p = userInfo.split(":", 2);
            config.setBasicUserName(p[0]);
            config.setBasicPassword(p[1]);
        }
        
        client = new XmlRpcClient();
        client.setConfig(config);
    }
    
    private void loadTransferClasses() {
        try {
            Object[] list = (Object[]) client.execute("getTransferClasses", new Object[0]);
            transferClasses = new TransferClass[list.length];
            
            for (int i = 0; i < list.length; i++) {
                Map<String,String> props = (Map<String,String>) list[i];
                transferClasses[i] = new TransferClass();
                
                transferClasses[i].setMode(props.get("mode"));
                transferClasses[i].setShortName(props.get("shortName"));
                transferClasses[i].setLongName(props.get("longName"));
            }
        } catch (XmlRpcException ex) {
            JOptionPane.showMessageDialog(rootPane, ex);
        }
    }
    
    @Override
    public Icon loadIcon(String path) {
        if (runAsStandalone)
            return new ImageIcon(".."+path);
        
        return new ImageIcon(getImage(getDocumentBase(), path));
    }
    
    private void reload() {
        queueModel.refresh();
        transferModel.refresh();
        
        if (queues.getSelectedIndex() == -1 && queueModel.getSize() > 0)
            queues.setSelectedIndex(0);
        
        updateUi();
        
        int curTab = mainTab.getSelectedIndex();
        if (curTab == 2 || curTab == 3)
            updateGraph();
        
    }

    private void updateUi() {
        boolean qempty = queueModel.getSize() == 0;
        int tsel = transfers.getSelectedRowCount();
        String state = "";
        
        if (tsel == 1)
            state = transferModel.getData(transfers.getSelectedRow()).get("state").toString();
        
        mainTab.setEnabledAt(1, tsel == 1);
        mainTab.setEnabledAt(2, tsel == 1);
        mainTab.setEnabledAt(3, !qempty);
        
        actionResume.setEnabled(tsel > 1 || (!state.equals("Active") && !state.equals("Waiting")));
        //buttonResume.setEnabled(tsel > 1 || (!state.equals("Active") && !state.equals("Waiting")));
        actionForceResume.setEnabled(tsel > 1 || !state.equals("ForcedActive"));
        //buttonForceResume.setEnabled(tsel > 1 || !state.equals("ForcedActive"));
        actionPause.setEnabled(tsel > 1 || !state.equals("Paused"));
        //buttonPause.setEnabled(tsel > 1 || !state.equals("Paused"));
        actionMoveToTop.setEnabled(tsel > 0);
        //buttonMoveToTop.setEnabled(tsel > 0);
        actionMoveUp.setEnabled(tsel > 0);
        //buttonMoveUp.setEnabled(tsel > 0);
        actionMoveDown.setEnabled(tsel > 0);
        //buttonMoveDown.setEnabled(tsel > 0);
        actionMoveToBottom.setEnabled(tsel > 0);
        //buttonMoveToBottom.setEnabled(tsel > 0);
        
        actionRemove.setEnabled(tsel > 0);
        actionRemoveWithData.setEnabled(tsel > 0);
        
        if (!mainTab.isEnabledAt(mainTab.getSelectedIndex()))
            mainTab.setSelectedIndex(0);
        
        transferLog.setEnabled(tsel == 1);
    }
    
    static public void main (String argv[]) {
        try {
            final FatRatApplet applet = new FatRatApplet();
            System.runFinalizersOnExit(true);
            Frame frame = new Frame("FatRat Remote Control");
            frame.addWindowListener(new WindowAdapter() {
                @Override
              public void windowClosing(WindowEvent event) {
                applet.stop();
                applet.destroy();
                System.exit(0);
              }
            });
            
            frame.add("Center", applet);
            applet.setStub (new MyAppletStub(argv, applet));
            frame.show();
            
            applet.standaloneVirtualURL = new URL(argv[0]);
            applet.runAsStandalone = true;
            applet.init();
            applet.start();
            frame.pack();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void reloadData() {
        SwingWorker worker = new SwingWorker<Void,Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Load queue list
                    System.out.println("Loading");
                    Object data = client.execute("getQueues", Collections.EMPTY_LIST);
                    queueModel.setData((Object[]) data);
                    
                    int sel = queues.getSelectedIndex();
                    System.out.println("Selected queue: "+sel);
                    if (sel != -1) {
                        data = client.execute("Queue.getTransfers", new String[] {queueModel.getData(sel).get("uuid").toString()});
                        transferModel.setData((Object[]) data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }

                return null;
            }

            @Override
            protected void done() {
                FatRatApplet.this.reload();
            }

        };

        worker.run();
    }
    
    private void updateGraph() {
        final boolean updateq = mainTab.getSelectedIndex() == 3;
        final boolean updatet = mainTab.getSelectedIndex() == 2;
        
        SwingWorker worker = new SwingWorker<String,Void>() {

            @Override
            protected String doInBackground() throws Exception {
                if (updateq) {
                    int selq = queues.getSelectedIndex();
                    String quuid = queueModel.getData(selq).get("uuid").toString();
                    
                    return client.execute("Queue.getSpeedData", new String[]{ quuid }).toString();
                } else if (updatet) {
                    int selt = transfers.getSelectedRow();
                    String tuuid = transferModel.getData(selt).get("uuid").toString();
                    
                    return client.execute("Transfer.getSpeedData", new String[]{ tuuid }).toString();
                } else
                    return null;
            }

            @Override
            protected void done() {
                try {
                    String data = get();
                    String[] pairs = data.split(";");
                    SpeedGraph.DataSample[] samples = new SpeedGraph.DataSample[pairs.length];
                    
                    if (data.equals(";"))
                        samples = null;
                    
                    for (int i = 0; i < pairs.length; i++) {
                        String[] v = pairs[i].split(",");
                        if (v == null || v.length != 2)
                            continue;
                        
                        samples[i] = new SpeedGraph.DataSample();
                        samples[i].down = Integer.parseInt(v[0]);
                        samples[i].up = Integer.parseInt(v[1]);
                    }
                    
                    if (updateq)
                        graphQueue.setData(samples);
                    else if (updatet)
                        graphTransfer.setData(samples);
                } catch (Exception ex) {
                    Logger.getLogger(FatRatApplet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }
    
    private void updateLogs() {
        final int t = transfers.getSelectedRow();
        
        SwingWorker worker = new SwingWorker<String[],Void>() {

            @Override
            protected String[] doInBackground() throws Exception {
                String[] rv = new String[2];
                URL src = new URL(baseURL + "/log");
                
                URLConnection conn = src.openConnection();
                setAuthFromURL(conn, src);
                InputStream is = conn.getInputStream();
                
                rv[0] = new String(IOUtils.toCharArray(is, "UTF-8"));
                is.close();
                
                if (t != -1) {
                    src = new URL(baseURL + "/log/" + transferModel.getData(t).get("uuid"));
                    conn = src.openConnection();
                    
                    setAuthFromURL(conn, src);
                    
                    is = conn.getInputStream();
                    rv[1] = new String(IOUtils.toCharArray(is, "UTF-8"));
                    is.close();
                }
                
                return rv;
            }

            @Override
            protected void done() {
                try {
                    String[] data = get();
                    
                    String orig = lastGlobalLog;
                    if (orig != null && data[0].startsWith(orig)) {
                        if (data[0].length() > orig.length()) {
                            globalLog.append(data[0].substring(orig.length()));
                        }
                    } else
                        globalLog.setText(data[0]);
                    lastGlobalLog = data[0];
                    
                    orig = lastTransferLog;
                    if (data[1] == null) {
                        transferLog.setText(null);
                        transferLog.setEnabled(false);
                        lastTransferLog = null;
                    } else if (orig != null && data[1].startsWith(orig)) {
                        if (data[1].length() > orig.length()) {
                            transferLog.setEnabled(true);
                            transferLog.append(data[1].substring(orig.length()));
                            lastTransferLog = data[1];
                        }
                    } else {
                        transferLog.setEnabled(true);
                        transferLog.setText(data[1]);
                        lastTransferLog = data[1];
                    }
                } catch (Exception ex) {
                    Logger.getLogger(FatRatApplet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        };
        worker.execute();
    }

    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolbar = new javax.swing.JToolBar();
        actionAdd = new javax.swing.JButton();
        buttonDelete = new javax.swing.JButton();
        buttonDeleteWithData = new javax.swing.JButton();
        buttonRemoveCompleted = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        buttonResume = new javax.swing.JButton();
        buttonForceResume = new javax.swing.JButton();
        buttonPause = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        buttonMoveToTop = new javax.swing.JButton();
        buttonMoveUp = new javax.swing.JButton();
        buttonMoveDown = new javax.swing.JButton();
        buttonMoveToBottom = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jLabel2 = new javax.swing.JLabel();
        refreshInterval = new javax.swing.JComboBox();
        mainTab = new javax.swing.JTabbedPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        queues = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        transfers = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        transferLog = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        globalLog = new javax.swing.JTextArea();

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        actionAdd.setToolTipText("Add");
        actionAdd.setFocusable(false);
        actionAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        actionAdd.setMargin(new java.awt.Insets(2, 2, 2, 2));
        actionAdd.setMaximumSize(new java.awt.Dimension(24, 24));
        actionAdd.setMinimumSize(new java.awt.Dimension(24, 24));
        actionAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        actionAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionAddActionPerformed(evt);
            }
        });
        toolbar.add(actionAdd);

        buttonDelete.setAction(actionRemove);
        buttonDelete.setToolTipText("Remove");
        buttonDelete.setFocusable(false);
        buttonDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonDelete.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonDelete.setMaximumSize(new java.awt.Dimension(24, 24));
        buttonDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionRemove(evt);
            }
        });
        toolbar.add(buttonDelete);

        buttonDeleteWithData.setAction(actionRemoveWithData);
        buttonDeleteWithData.setToolTipText("Remove with data");
        buttonDeleteWithData.setFocusable(false);
        buttonDeleteWithData.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonDeleteWithData.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonDeleteWithData.setMaximumSize(new java.awt.Dimension(24, 24));
        buttonDeleteWithData.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonDeleteWithData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionRemove(evt);
            }
        });
        toolbar.add(buttonDeleteWithData);

        buttonRemoveCompleted.setToolTipText("Remove completed");
        buttonRemoveCompleted.setFocusable(false);
        buttonRemoveCompleted.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonRemoveCompleted.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonRemoveCompleted.setMaximumSize(new java.awt.Dimension(24, 24));
        buttonRemoveCompleted.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonRemoveCompleted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveCompleted(evt);
            }
        });
        toolbar.add(buttonRemoveCompleted);
        toolbar.add(jSeparator1);

        buttonResume.setAction(actionResume);
        buttonResume.setToolTipText("Resume");
        buttonResume.setHideActionText(true);
        buttonResume.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonResume.setMaximumSize(new java.awt.Dimension(24, 24));
        toolbar.add(buttonResume);

        buttonForceResume.setAction(actionForceResume);
        buttonForceResume.setToolTipText("Force resume");
        buttonForceResume.setHideActionText(true);
        buttonForceResume.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonForceResume.setMaximumSize(new java.awt.Dimension(24, 24));
        toolbar.add(buttonForceResume);

        buttonPause.setAction(actionPause);
        buttonPause.setToolTipText("Pause");
        buttonPause.setHideActionText(true);
        buttonPause.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonPause.setMaximumSize(new java.awt.Dimension(24, 24));
        toolbar.add(buttonPause);
        toolbar.add(jSeparator2);

        buttonMoveToTop.setAction(actionMoveToTop);
        buttonMoveToTop.setToolTipText("Move to top");
        buttonMoveToTop.setHideActionText(true);
        buttonMoveToTop.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonMoveToTop.setMaximumSize(new java.awt.Dimension(24, 24));
        buttonMoveToTop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionMoveTransfer(evt);
            }
        });
        toolbar.add(buttonMoveToTop);

        buttonMoveUp.setAction(actionMoveUp);
        buttonMoveUp.setToolTipText("Move up");
        buttonMoveUp.setHideActionText(true);
        buttonMoveUp.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonMoveUp.setMaximumSize(new java.awt.Dimension(24, 24));
        buttonMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionMoveTransfer(evt);
            }
        });
        toolbar.add(buttonMoveUp);

        buttonMoveDown.setAction(actionMoveDown);
        buttonMoveDown.setToolTipText("Move down");
        buttonMoveDown.setHideActionText(true);
        buttonMoveDown.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonMoveDown.setMaximumSize(new java.awt.Dimension(24, 24));
        buttonMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionMoveTransfer(evt);
            }
        });
        toolbar.add(buttonMoveDown);

        buttonMoveToBottom.setAction(actionMoveToBottom);
        buttonMoveToBottom.setToolTipText("Move to bottom");
        buttonMoveToBottom.setHideActionText(true);
        buttonMoveToBottom.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonMoveToBottom.setMaximumSize(new java.awt.Dimension(24, 24));
        buttonMoveToBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionMoveTransfer(evt);
            }
        });
        toolbar.add(buttonMoveToBottom);
        toolbar.add(filler1);

        jLabel2.setText("Refresh every");
        toolbar.add(jLabel2);

        refreshInterval.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2 seconds", "5 seconds", "15 seconds", "30 seconds" }));
        refreshInterval.setMaximumSize(new java.awt.Dimension(100, 32767));
        refreshInterval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshIntervalChanged(evt);
            }
        });
        toolbar.add(refreshInterval);

        mainTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mainTabStateChanged(evt);
            }
        });

        jSplitPane1.setDividerLocation(150);

        queues.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        queues.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                queuesValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(queues);

        jSplitPane1.setLeftComponent(jScrollPane1);

        transfers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Progress", "Size", "Speed d", "Speed u", "Time remaining", "Message"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        transfers.setRowHeight(24);
        transfers.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        transfers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                transfersMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(transfers);

        jSplitPane1.setRightComponent(jScrollPane2);

        mainTab.addTab("Transfers", jSplitPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 834, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 489, Short.MAX_VALUE)
        );

        mainTab.addTab("Details", jPanel1);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        jSplitPane2.setTopComponent(jScrollPane3);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane4.setViewportView(jTextArea2);

        jSplitPane2.setRightComponent(jScrollPane4);

        jLabel1.setText("jLabel1");
        jSplitPane2.setTopComponent(jLabel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Transfer log");
        jLabel3.setMaximumSize(new java.awt.Dimension(9999, 15));
        jPanel2.add(jLabel3, java.awt.BorderLayout.PAGE_START);

        transferLog.setColumns(20);
        transferLog.setEditable(false);
        transferLog.setRows(5);
        jScrollPane5.setViewportView(transferLog);

        jPanel2.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jSplitPane2.setTopComponent(jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Global log");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel4.setMaximumSize(new java.awt.Dimension(9999, 15));
        jPanel3.add(jLabel4, java.awt.BorderLayout.PAGE_START);

        globalLog.setColumns(20);
        globalLog.setEditable(false);
        globalLog.setRows(5);
        jScrollPane6.setViewportView(globalLog);

        jPanel3.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jSplitPane2.setBottomComponent(jPanel3);

        mainTab.addTab("Log", jSplitPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
            .addComponent(mainTab, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainTab, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void queuesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_queuesValueChanged
        updateUi();
        reloadData();
    }//GEN-LAST:event_queuesValueChanged

    private void buttonRemoveCompleted(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveCompleted
        List<String> uuids = new ArrayList<String>();
        int[] ts = transfers.getSelectedRows();
        
        for (int t : ts) {
            Map<String,Object> data = transferModel.getData(t);
            String curState = data.get("state").toString();
            
            if (curState.equals("Completed")) {
                String uuid = data.get("uuid").toString();
                uuids.add(uuid);
            }
        }
        
        removeTransfers(uuids, false);
    }//GEN-LAST:event_buttonRemoveCompleted

    private void actionRemove(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionRemove
        Object src = evt.getSource();
        boolean withData = false;
        
        if (src.equals(buttonDeleteWithData))
            withData = true;
        
        String question;
        if (withData)
            question = "Do you really want to delete the selected transfers including the data?";
        else
            question = "Do you really want to delete the selected transfers?";
        
        if (JOptionPane.showConfirmDialog(rootPane, question, "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;
        
        removeTransfers(withData);
    }//GEN-LAST:event_actionRemove

    public void removeTransfers(boolean withData) {
        List<String> uuids = new ArrayList<String>(transfers.getSelectedRowCount());
        int[] ts = transfers.getSelectedRows();
        
        for (int t : ts)
            uuids.add(transferModel.getData(t).get("uuid").toString());
        
        removeTransfers(uuids, withData);
    }
    
    public void moveTransfer(String dir) {
        int qsel = queues.getSelectedIndex();
        int[] tsel = transfers.getSelectedRows();
        
        if (qsel == -1 || tsel.length == 0)
            return;
        
        final String quuid = queueModel.getData(qsel).get("uuid").toString();
        final String[] tuuid = new String[tsel.length];
        final String fdir = dir;
        
        for (int i = 0; i < tsel.length; i++)
            tuuid[i] = transferModel.getData(tsel[i]).get("uuid").toString();
        
        SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                client.execute("Queue.moveTransfers", new Object[] {
                    quuid, tuuid, fdir
                });
                return null;
            }

            @Override
            protected void done() {
                reloadData();
                
                ListSelectionModel model = transfers.getSelectionModel();
                Object[] data = transferModel.getData();
                
                model.clearSelection();
                Arrays.sort(tuuid);
                
                for (int i = 0; i < data.length; i++) {
                    Map<String,Object> m = (Map<String,Object>) data[i];
                    String uuid = m.get("uuid").toString();
                    
                    if (Arrays.binarySearch(tuuid, uuid) >= 0)
                        model.addSelectionInterval(i, i);
                }
            }
            
        };
        worker.execute();
    }
    private void actionMoveTransfer(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionMoveTransfer
        Object src = evt.getSource();
        String dir;
        
        if (src.equals(buttonMoveToTop))
            dir = "top";
        else if (src.equals(buttonMoveUp))
            dir = "up";
        else if (src.equals(buttonMoveDown))
            dir = "down";
        else if (src.equals(buttonMoveToBottom))
            dir = "bottom";
        else
            return;
        
        moveTransfer(dir);
    }//GEN-LAST:event_actionMoveTransfer

    private void refreshIntervalChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshIntervalChanged
        try {
            int i = refreshInterval.getSelectedIndex();
            int secs = refreshIntervals[i];
            timer.setDelay(secs*1000);
        
            settings.setValue("refreshInterval", i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_refreshIntervalChanged

    private void mainTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mainTabStateChanged
        if (mainTab.getSelectedIndex() != lastTabIndex) {
            lastTabIndex = mainTab.getSelectedIndex();
            reloadData();
        }
    }//GEN-LAST:event_mainTabStateChanged

    private void transfersMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_transfersMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            transferPopupMenu.show(transfers, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_transfersMouseClicked

    private void actionAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAddActionPerformed
        NewTransfer dlg = new NewTransfer(Frame.getFrames()[0], true);
        
        dlg.setQueueModel(queueModel);
        dlg.setTransferClasses(transferClasses);
        dlg.setVisible(true);
        
        if (dlg.getReturnStatus() != NewTransfer.RET_OK)
            return;
        
        List<String> urls = new LinkedList<String>(Arrays.asList(dlg.getURLs()));
        List<File> files = new ArrayList<File>();
        
        if (urls.isEmpty())
            return;
        
        // Files need to be transferred separately
        for (Iterator<String> it = urls.iterator(); it.hasNext(); ) {
            String url = it.next();
            if (url.startsWith("local://")) {
                files.add(new File(url.substring(8)));
                it.remove();
            }
        }
        
        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          
            // Send URLs
            if (!urls.isEmpty()) {
                Object[] msg = new Object[8];
                msg[0] = false; // Download
                msg[1] = dlg.getQueueUUID();
                msg[2] = urls.toArray(new String[urls.size()]);
                msg[3] = dlg.getTransferClass();
                msg[4] = dlg.getTargetDirectory();
                msg[5] = dlg.addPaused();
                msg[6] = dlg.getDownSpeedLimit();
                msg[7] = dlg.getUpSpeedLimit();
                
                client.execute("Queue.addTransfers", msg);
            }
            
            // Send files
            for (File f : files) {
                Object[] msg = new Object[9];
                msg[0] = false; // Download
                msg[1] = dlg.getQueueUUID();
                msg[2] = f.getName();
                msg[3] = IOUtils.toByteArray(new FileInputStream(f));
                msg[4] = dlg.getTransferClass();
                msg[5] = dlg.getTargetDirectory();
                msg[6] = dlg.addPaused();
                msg[7] = dlg.getDownSpeedLimit();
                msg[8] = dlg.getUpSpeedLimit();
                
                client.execute("Queue.addTransferWithData", msg);
            }
            
            reloadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, e, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_actionAddActionPerformed

    public void changeState(String state) {
        final List<String> uuid1 = new ArrayList<String>(transfers.getSelectedRowCount());
        final List<String> uuid2 = new ArrayList<String>();
        
        int[] ts = transfers.getSelectedRows();
        for (int t : ts) {
            Map<String,Object> data = transferModel.getData(t);
            String uuid = data.get("uuid").toString();
            
            if (state.equals("Active")) {
                String curState = data.get("state").toString();
                if (!curState.equals("Active") && !curState.equals("ForcedActive"))
                    uuid2.add(uuid);
                else
                    uuid1.add(uuid);
            } else {
                uuid1.add(uuid);
            }
        }
        
        if (!uuid1.isEmpty() || !uuid2.isEmpty()) {
            final String state1 = state;
            SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (!uuid1.isEmpty()) {
                        client.execute("Transfer.setProperties", new Object[]{
                            uuid1, Collections.singletonMap("state", state1)
                        });
                    }
                    
                    if (!uuid2.isEmpty()) {
                        client.execute("Transfer.setProperties", new Object[]{
                            uuid2, Collections.singletonMap("state", "Waiting")
                        });
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    reloadData();
                }
            };
            worker.execute();
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionAdd;
    private javax.swing.JButton buttonDelete;
    private javax.swing.JButton buttonDeleteWithData;
    private javax.swing.JButton buttonForceResume;
    private javax.swing.JButton buttonMoveDown;
    private javax.swing.JButton buttonMoveToBottom;
    private javax.swing.JButton buttonMoveToTop;
    private javax.swing.JButton buttonMoveUp;
    private javax.swing.JButton buttonPause;
    private javax.swing.JButton buttonRemoveCompleted;
    private javax.swing.JButton buttonResume;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JTextArea globalLog;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTabbedPane mainTab;
    private javax.swing.JList queues;
    private javax.swing.JComboBox refreshInterval;
    private javax.swing.JToolBar toolbar;
    private javax.swing.JTextArea transferLog;
    private javax.swing.JTable transfers;
    // End of variables declaration//GEN-END:variables

    private void removeTransfers(final List<String> uuids, final boolean withData) {
        SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                client.execute("Transfer.delete", new Object[] {
                    uuids, withData
                });
                return null;
            }
            
            @Override
            protected void done() {
                transfers.getSelectionModel().clearSelection();
                reloadData();
            }
        };
        worker.execute();
    }

    private class RegularUpdateListener implements ActionListener {

        public RegularUpdateListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            reloadData();
            updateLogs();
        }
    }
    
    class TransferSelectionListener implements ListSelectionListener {

        public TransferSelectionListener() {
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            updateUi();
            graphTransfer.setData(null);
        }
    }
    
    private static void setAuthFromURL(URLConnection conn, URL url) throws UnsupportedEncodingException {
        String auth = url.getUserInfo();
        
        if (auth != null) {
            auth = Base64.encodeBase64String(auth.getBytes("UTF-8"));

            conn.setRequestProperty("Authorization", "Basic " + auth);
        }
    }

    public AppletSettings getSettings() {
        return settings;
    }

    public XmlRpcClient getClient() {
        return client;
    }
    
    public Map<String,Object> getCurrentTransfer() {
        int sel = transfers.getSelectedRow();
        if (sel < 0)
            return null;
        return transferModel.getData(sel);
    }
    
    class StateAction extends AbstractAction {

        public StateAction(String name, Icon icon) {
            super(name, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            changeState(e.getActionCommand());
        }
    }

    class MoveAction extends AbstractAction {

        public MoveAction(String name, Icon icon) {
            super(name, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            moveTransfer(e.getActionCommand());
        }
    }

}
