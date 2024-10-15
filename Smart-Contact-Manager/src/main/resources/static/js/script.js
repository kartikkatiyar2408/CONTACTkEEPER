console.log("This is javaScript");

const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
  }
};

const search = () => {
  // console.log("searching...");
  let query = $("#search-input").val();
  console.log(query);

  if (query == "") {
    $(".search-result").hide();
  } else {
    console.log(query);

    //sending request
    let url = `http://localhost:8080/search/${query}`;

    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        let text = `<div class= 'list-group'>`;

        data.forEach((contact) => {
          text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`;
        });

        text += `</div>`;

        $(".search-result").html(text);

        $(".search-result").show();
      });
  }
};

//first req to server to create order
const paymentStart = () => {
  let amount = $("#paymentAmount").val();
  if (amount == "" || amount == null) {
    // alert("Amount is required !!");
    swal("Failed!!", "Amount is required!!", "error");
    return;
  }

  //using ajax to send req to server to  create order
  console.log("Sending request");
  $.ajax({
    url: "/user/createOrder",
    data: JSON.stringify({ amount: amount, info: "orderRequest" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      console.log(response);

      if (response.status == "created") {
        //open payment form
        let options = {
          key: "rzp_test_5ac7GHpxal0BcP",
          amount: response.amount,
          currency: "INR",
          name: "Contact Manager Donation",
          description: "Donation",
          image:
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSG2Hm0y5skrG82aExSqPLIOae8v75QdvG9UA&usqp=CAU",
          order_id: response.id,
          handler: function (response) {
            console.log(response.razorpay_payment_id);
            console.log(response.razorpay_order_id);
            console.log(response.razorpay_signature);
            console.log("payment successfull...");

            updatePaymentOnServer(
              response.razorpay_payment_id,
              response.razorpay_order_id,
              "paid"
            );
          },
          prefill: {
            name: "",
            email: "",
            contact: "",
          },
          notes: {
            address: "Dipendra",
          },
          theme: {
            color: "#3399cc",
          },
        };

        let rzp = new Razorpay(options);

        rzp.on("payment.failed", function (response) {
          console.log(response.error.description);
          console.log(response.error.code);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);
          swal("Failed!!", "Oops payment failed !!", "error");
        });

        rzp.open();
      }
    },
    error: function (error) {
      console.log(error);
      alert("Something went wrong");
    },
  });
};

function updatePaymentOnServer(payment_id, order_id, status){
  $.ajax({
    url: "/user/updateOrder",
    data: JSON.stringify({
      paymentId: payment_id,
      orderId: order_id,
      status: status,
    }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    
    success:function(response){
      swal("Good job!!", "Payment Successful !!", "success");
    },
    error:function(error){
      swal("Failed !!", "Payment is successful, But can't get it !!", "error");
    },
  });
}
