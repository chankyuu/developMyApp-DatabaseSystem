import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MetaData{
	private Connection con = MyConnection.mackConnection();
	private TreeView<String> tree;
	private Label[] labels;
	private TextField[] txt;
	
	public void getStage() {
		Stage stage = new Stage();
		VBox vbox = new VBox();
		vbox.setSpacing(10);
		vbox.setMinSize(800, 600);
		vbox.setMaxSize(1000, 800);
		vbox.setPadding(new Insets(15,15,15,15));
		
		// add Top Pane
		BorderPane tbox = addTopPane();
		tbox.prefHeightProperty().bind(vbox.prefWidthProperty());
		vbox.getChildren().add(tbox);
		
		// add Center box
		HBox cbox = addCenterPane();
		cbox.prefHeightProperty().bind(vbox.prefWidthProperty());
		vbox.getChildren().add(cbox);
		
		Scene scene = new Scene(vbox);
		stage.setScene(scene);
		stage.setTitle("Database Meta Data");
		stage.show();
	}
	
	private BorderPane addTopPane() {
		BorderPane bPane = new BorderPane();
		
		bPane.setPadding(new Insets(15,15,50,15));
		Text text = new Text("About my Database");
		text.setFont(Font.font("Arial", FontWeight.BOLD, 36));
		
		bPane.setCenter(text);
		
		return bPane;
	}
	
	private HBox addCenterPane() {
		HBox hb1 = new HBox();
		
		// Add TableVIew
		VBox vb = new VBox();
		
		// Add Labels and TextFields
		GridPane gp = new GridPane();
		gp.setPadding(new Insets(50,15,15,20));
		gp.setHgap(10);
		gp.setVgap(10);
		gp.setStyle("-fx-border-color: Blue;");
		// gp.prefHeightProperty().bind(tree.prefHeightProperty());
		txt = new TextField[10];
		labels = new Label[10];
		
		for(int i = 0; i < labels.length; i++) {
			labels[i] = new Label("Label..");
			labels[i].setFont(Font.font("Arial", FontWeight.BOLD, 14));
			labels[i].setMinSize(150,  40);
			txt[i] = new TextField(" Text.. ");
			txt[i].setMinSize(300,  40);
			txt[i].setEditable(false);
			txt[i].setFont(Font.font("Arial", FontWeight.BOLD, 14));
			gp.addRow(i,  labels[i], txt[i]);
			labels[i].prefHeightProperty().bind(gp.widthProperty());
			txt[i].prefHeightProperty().bind(gp.widthProperty());
		}
		
		// you will write this function
		fillGrid();
		
		vb.getChildren().addAll(gp);
		
		// Add TreeView
		StackPane stack = new StackPane();
		
		// Create the tree
		tree = addNodestoTree();	// you will write
		tree.setShowRoot(true);
		
		tree.setMaxWidth(400);
		tree.prefWidthProperty().bind(stack.prefWidthProperty());
		stack.getChildren().add(tree);
		
		hb1.getChildren().addAll(stack, vb);
		hb1.setStyle("-fx-border-color:black;");
		hb1.setSpacing(20);
		hb1.prefHeightProperty().bind(vb.prefWidthProperty());
	
		return hb1;
	}

	private TreeView<String> addNodestoTree(){
		TreeView<String> tree = new TreeView<String>();
		TreeItem<String> root, tables, sfunction, tfunctions, sprocedures, triggers;
		
		root = new TreeItem<String>("ConvenienceStore");
		tree.setRoot(root);
		
		tables = new TreeItem<String>(Nodes02.Tables.toString());
		try {
			String sql1 = "select object_name as tn from listofTable()";
			ResultSet rs1 = (con.prepareStatement(sql1)).executeQuery();
			int i = 0;
			while(rs1.next()) {
				String tname = rs1.getString("tn");
				makeChild(tname, tables);
				
				String sql2 = "select * from getColumnsList(?)";
				PreparedStatement pst = con.prepareStatement(sql2);
				pst.setString(1,  tname);
				ResultSet rs2 = pst.executeQuery();
				
				while(rs2.next()) {
					String cnames = rs2.getString("column_name") + "(" + rs2.getString("column_type") + "," + rs2.getString("max_length") + ")";
							
					makeChild(cnames, tables.getChildren().get(i));
				}
				i++;
				rs2.close();
			}
			rs1.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {}
		sfunction = new TreeItem<String>(Nodes02.Scalar_Functions.toString());
		// add Scalar valued function and their parameters
		try {
			String sql_scalar = "select name as sf from dbo.getScalarFunctions()";
			ResultSet rs_scalar = (con.prepareStatement(sql_scalar)).executeQuery();
			int i = 0;
			while(rs_scalar.next()) {
				String tname = rs_scalar.getString("sf");
				makeChild(tname, sfunction);
				
				String sql2 = "select * from getParameters(?)";
				PreparedStatement pst = con.prepareStatement(sql2);
				pst.setString(1,  tname);
				ResultSet rs2 = pst.executeQuery();
				
				while(rs2.next()) {
					String cnames = rs2.getString("parameter_name") + "(" + rs2.getString("parameter_type") + "," + rs2.getString("max_length") + ")";
					makeChild(cnames, sfunction.getChildren().get(i));
				}
				i++;
				rs2.close();
			}
			rs_scalar.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {}
		
		tfunctions = new TreeItem<String>(Nodes02.Table_Functions.toString());
		// add Table valued function and their parameters
		try {
			String sql_table = "select name as tf from dbo.getTableFunctions()";
			ResultSet rs_table = (con.prepareStatement(sql_table)).executeQuery();
			int i = 0;
			while(rs_table.next()) {
				String tname = rs_table.getString("tf");
				makeChild(tname, tfunctions);
				
				String sql2 = "select * from getParameters(?)";
				PreparedStatement pst = con.prepareStatement(sql2);
				pst.setString(1,  tname);
				ResultSet rs2 = pst.executeQuery();
				
				while(rs2.next()) {
					String cnames = rs2.getString("parameter_name") + "(" + rs2.getString("parameter_type") + "," + rs2.getString("max_length") + ")";
					makeChild(cnames, tfunctions.getChildren().get(i));
				}
				i++;
				rs2.close();
			}
			rs_table.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {}
		sprocedures = new TreeItem<String>(Nodes02.Stored_Procedures.toString());
		// add Stored procedures and their parameters
		try {
			String sql_sp = "select name as sp from dbo.getStoredProceduresFunctions()";
			ResultSet rs_sp = (con.prepareStatement(sql_sp)).executeQuery();
			int i = 0;
			while(rs_sp.next()) {
				String tname = rs_sp.getString("sp");
				makeChild(tname, sprocedures);
				
				String sql2 = "select * from getParameters(?)";
				PreparedStatement pst = con.prepareStatement(sql2);
				pst.setString(1,  tname);
				ResultSet rs2 = pst.executeQuery();
				
				while(rs2.next()) {
					String cnames = rs2.getString("parameter_name") + "(" + rs2.getString("parameter_type") + "," + rs2.getString("max_length") + ")";
					makeChild(cnames, sprocedures.getChildren().get(i));
				}
				i++;
				rs2.close();
			}
			rs_sp.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {}
		triggers = new TreeItem<String>(Nodes02.Triggers.toString());
		// add Triggers (트리거는 매개변수가 없음)
		try {
			String sql_trr = "select name as trr from dbo.getTriggers()";
			ResultSet rs_trr = (con.prepareStatement(sql_trr)).executeQuery();
			int i = 0;
			while(rs_trr.next()) {
				String tname = rs_trr.getString("trr");
				makeChild(tname, triggers);
				
			}
			rs_trr.close();
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {}
			
		root.getChildren().addAll(tables, sfunction, tfunctions, sprocedures, triggers);
			
		return tree;
	}
	
	// Create child
	private TreeItem<String> makeChild(String title, TreeItem<String> parent) {
		TreeItem<String> item = new TreeItem<>(title);
		item.setExpanded(false);
		parent.getChildren().add(item);
		return item;
	}
	
	private void fillGrid() {
		try {
			DatabaseMetaData dm = (DatabaseMetaData) con.getMetaData();
			
			labels[0].setText("Database name ");
			String sql0 = "select db_name() as dn";
			PreparedStatement ps0 = con.prepareStatement(sql0);
			ResultSet rs0 = ps0.executeQuery();
			rs0.next();
			txt[0].setText(rs0.getString("dn"));
			//txt[0].setText(con.getCatalog());
			
			labels[1].setText("Number of Tables ");
			ResultSet rs1 = (con.prepareStatement("select dbo.numTables() as nt")).executeQuery();
			rs1.next();
			txt[1].setText(rs1.getString("nt"));
			rs1.close();
			
			labels[2].setText("Number of Scalar Values Functions ");
			ResultSet rs2 = (con.prepareStatement("select count(*) as sf from dbo.getScalarFunctions()")).executeQuery();
			rs2.next();
			txt[2].setText(rs2.getString("sf"));
			rs2.close();
			
			labels[3].setText("Number of Table-Valued Functions");
			ResultSet rs3 = (con.prepareStatement("select count(*) as tf from dbo.getTableFunctions()")).executeQuery();
			rs3.next();
			txt[3].setText(rs3.getString("tf"));
			rs3.close();
			
			labels[4].setText("Number of Stored procedures ");
			ResultSet rs4 = (con.prepareStatement("select count(*) as stf from dbo.getStoredProceduresFunctions()")).executeQuery();
			rs4.next();
			txt[4].setText(rs4.getString("stf"));
			rs4.close();
			
			labels[5].setText("Number of Triggers ");
			ResultSet rs5 = (con.prepareStatement("select count(*) as trr from dbo.getTriggers()")).executeQuery();
			rs5.next();
			txt[5].setText(rs5.getString("trr"));
			rs5.close();
			
			labels[6].setText("DBMS name: ");
			txt[6].setText(dm.getDatabaseProductName());
			
			labels[7].setText("DBMS Version: ");
			txt[7].setText(dm.getDatabaseProductVersion());
			
			labels[8].setText("JDBC Driver name: " );
			txt[8].setText(dm.getDriverName());
			
			labels[9].setText("JDBC Driver Version: ");
			txt[9].setText(dm.getDriverVersion());
		
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		} finally {}
	}
}