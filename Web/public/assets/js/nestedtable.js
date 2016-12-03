
function fnFormatDetails(table_id, html) {
    var sOut = "<table id=\"sample_2_" + table_id + "\">";
    sOut += html;
    sOut += "</table>";
    return sOut;
}

//////////////////////////////////////////////////////////// EXTERNAL DATA - Array of Objects


// DETAILS ROW A
var detailsRowAPlayer1 = { pic: "<?php echo base_url();?>assets/photo/plus.png", name: "Jaedong", team: "evil geniuses", server: "NA" };
var detailsRowAPlayer2 = { pic: "<?php echo base_url();?>assets/photo/plus.png", name: "Scarlett", team: "acer", server: "Europe" };
var detailsRowAPlayer3 = { pic: "<?php echo base_url();?>assets/photo/plus.png", name: "Stephano", team: "evil geniuses", server: "Europe" };

var detailsRowA = [ detailsRowAPlayer1, detailsRowAPlayer2, detailsRowAPlayer3 ];

// DETAILS ROW B
var detailsRowBPlayer1 = { pic: "<?php echo base_url();?>assets/photo/plus.png", name: "Grubby", team: "independent", server: "Europe" };

var detailsRowB = [ detailsRowBPlayer1 ];

// DETAILS ROW C
var detailsRowCPlayer1 = { pic: '<?php echo base_url();?>assets/photo/plus.png', name: "Bomber", team: "independent", server: "NA" };

var detailsRowC = [ detailsRowCPlayer1 ];

var rowA = { Institution: "Telkom University", studentbody: "2014", teacher: "3",total: "3",ratio: "32", details: detailsRowA};
var rowB = { Institution: "Akademi Telekomunikasi", studentbody: "2014", teacher: "1",total: "354",ratio: "3", details: detailsRowB};
var rowC = { Institution: "ST3 Telkom", studentbody: "2014", teacher: "1" ,total: "3",ratio: "34",details: detailsRowC};

var newRowData = [rowA, rowB, rowC] ;

////////////////////////////////////////////////////////////

var iTableCounter = 1;
    var oTable;
    var oInnerTable;
    var detailsTableHtml;

    //Run On HTML Build
    $(document).ready(function () {

        // you would probably be using templates here
        detailsTableHtml = $("#sample_2").html();

        //Insert a 'details' column to the table
        var nCloneTh = document.createElement('th');
        var nCloneTd = document.createElement('td');
        nCloneTd.innerHTML = '<img src="<?php echo base_url();?>assets/photo/plus.png">';
        nCloneTd.className = "center";

        $('#sample_2 thead tr').each(function () {
            this.insertBefore(nCloneTh, this.childNodes[0]);
        });

        $('#sample_2 tbody tr').each(function () {
            this.insertBefore(nCloneTd.cloneNode(true), this.childNodes[0]);
        });


        //Initialse DataTables, with no sorting on the 'details' column
        var oTable = $('#sample_2').dataTable({
            "bJQueryUI": true,
            "aaData": newRowData,
            "bPaginate": false,
            "aoColumns": [
                {
                   "mDataProp": null,
                   "sClass": "control center",
                   "sDefaultContent": '<img src="<?php echo base_url();?>assets/photo/plus.png">'
                },
                { "mDataProp": "Institution" },
                { "mDataProp": "studentbody" },
                { "mDataProp": "teacher" },
                { "mDataProp": "total" },
                { "mDataProp": "ratio" }
            ],
            "oLanguage": {
			    "sInfo": "_teacher_ entries"
			},
            "aaSorting": [[1, 'asc']]
        });

        /* Add event listener for opening and closing details
        * Note that the indicator for showing which row is open is not controlled by DataTables,
        * rather it is done here
        */
        $('#sample_2 tbody td img').on('click', function () {
            var nTr = $(this).parents('tr')[0];
            var nTds = this;

            if (oTable.fnIsOpen(nTr)) {
                /* This row is already open - close it */
                this.src = "<?php echo base_url();?>assets/photo/plus.png";
                oTable.fnClose(nTr);
            }
            else {
                /* Open this row */
                var rowIndex = oTable.fnGetPosition( $(nTds).closest('tr')[0] );
	            var detailsRowData = newRowData[rowIndex].details;

                this.src = "<?php echo base_url();?>assets/photo/minus.png";
                oTable.fnOpen(nTr, fnFormatDetails(iTableCounter, detailsTableHtml), 'details');
                oInnerTable = $("#sample_2_" + iTableCounter).dataTable({
                    "bJQueryUI": true,
                    "bFilter": false,
                    "aaData": detailsRowData,
                    "bSort" : true, // disables sorting
                    "aoColumns": [
                    { "mDataProp": "pic" },
	                { "mDataProp": "name" },
	                { "mDataProp": "team" },
	                { "mDataProp": "server" }
	            ],
                    "bPaginate": false,
                    "oLanguage": {
						"sInfo": "_teacher_ entries"
			        },
                    "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
                      var imgLink = aData['pic'];
                      var imgTag = '<img  src="' + imgLink + '"/>';
                      $('td:eq(0)', nRow).html(imgTag);
                     return nRow;
                    }
                });
                iTableCounter = iTableCounter + 1;
            }
        });


    });
