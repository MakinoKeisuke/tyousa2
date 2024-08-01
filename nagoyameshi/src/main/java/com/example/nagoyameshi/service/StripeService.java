package com.example.nagoyameshi.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class StripeService {
	 @Value("${stripe.api-key}")
     private String stripeApiKey;
	 
	 private final MemberService memberService;
     
     public StripeService(MemberService memberService) {
         this.memberService= memberService;
     }    
     // セッションを作成し、Stripeに必要な情報を返す
     public String createStripeSession(String shopeName, ReservationRegisterForm reservationRegisterForm, HttpServletRequest httpServletRequest) {
    	 Stripe.apiKey = stripeApiKey;
         String requestUrl = new String(httpServletRequest.getRequestURL());
         System.out.println("aaaa");
         System.out.println(requestUrl);
      // nullチェックとデフォルト値の設定
         Integer numberOfPeople = reservationRegisterForm.getNumberOfPeople();
         if (numberOfPeople == null) {
             numberOfPeople = 1; // デフォルト値を設定
         }
         
         SessionCreateParams params =
             SessionCreateParams.builder()
                 .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                 .addLineItem(
                     SessionCreateParams.LineItem.builder()
                         .setPriceData(
                             SessionCreateParams.LineItem.PriceData.builder()   
                                 .setProductData(
                                     SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                         .setName(shopeName)
                                         .build())
                                 .setUnitAmount((long)300/*reservationRegisterForm.getAmount()*/)
                                 .setCurrency("jpy")                                
                                 .build())
                         .setQuantity(1L)
                         .build())
                 .setMode(SessionCreateParams.Mode.PAYMENT)
                 .setSuccessUrl(requestUrl)//replaceAll("/shope/[0-9]+/reservations/confirm", "") + "/reservations?reserved")
                 .setCancelUrl(requestUrl.replace("/reservations/confirm", ""))
                 .setPaymentIntentData(
                     SessionCreateParams.PaymentIntentData.builder()
                         .putMetadata("shopeId", reservationRegisterForm.getShopeId().toString())
                         .putMetadata("memberId", reservationRegisterForm.getMemberId().toString())
                         .putMetadata("reservationDate", reservationRegisterForm.getReservationDate())
                         .putMetadata("reservationTime", reservationRegisterForm.getReservationTime())
  //                       .putMetadata("numberOfPeople", reservationRegisterForm.getNumberOfPeople().toString())
                         .putMetadata("numberOfPeople", numberOfPeople.toString())
                         .putMetadata("amount", "300" /*reservationRegisterForm.getAmount().toString()*/)
                         .putMetadata("roleName", "ROLE_PAID_MEMBER")
                         .build())
                 .build();
         try {
             Session session = Session.create(params);
             return session.getId();
         } catch (StripeException e) {
             e.printStackTrace();
             return "";
         }
     } 
  // セッションから予約情報を取得し、ReservationServiceクラスを介してデータベースに登録する  
     public void processSessionCompleted(Event event) {
         Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
         optionalStripeObject.ifPresentOrElse(stripeObject -> {
             Session session = (Session)stripeObject;
             SessionRetrieveParams params = SessionRetrieveParams.builder().addExpand("payment_intent").build();
 
             try {
                 session = Session.retrieve(session.getId(), params, null);
                 Map<String, String> paymentIntentObject = session.getPaymentIntentObject().getMetadata();
                 memberService.update(paymentIntentObject);
             } catch (StripeException e) {
                 e.printStackTrace();
             }
             System.out.println("予約一覧ページの登録処理が成功しました。");
             System.out.println("Stripe API Version: " + event.getApiVersion());
             System.out.println("stripe-java Version: " + Stripe.VERSION);
         },
         () -> {
             System.out.println("予約一覧ページの登録処理が失敗しました。");
             System.out.println("Stripe API Version: " + event.getApiVersion());
             System.out.println("stripe-java Version: " + Stripe.VERSION);
         });
     }
}

/*@Service
public class StripeService {
	@Value("${stripe.api-key}")
    private String stripeApiKey;
	private final MemberService memberService;
    
    public StripeService(MemberService memberService) {
        this.memberService = memberService;
    }  
  
    public String createStripeSession(HttpServletRequest httpServletRequest,Member member) {
   	 String memberId = (member != null && member.getId() != null) ? String.valueOf(member.getId()) : "null";
   	 Stripe.apiKey = stripeApiKey;
     String requestUrl = new String(httpServletRequest.getRequestURL());

	
        
	 SessionCreateParams params = SessionCreateParams.builder()
                
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(requestUrl.replaceAll("/subscription/create", "") + "/login?reserved")
                .addLineItem(
          		      SessionCreateParams.LineItem.builder()
          		        .setPrice("price_1Pe3evDj1UaUVpmtBapEuo2m")
          		        .setQuantity(1L)
          		        .build()
          		    )
                .putMetadata("memberId", memberId)
                .putMetadata("roleName", "ROLE_PAID_MEMBER")
          		 
          		    .build();
        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            e.printStackTrace();
            return "";
        }
    } 
    public void processSessionCompleted(Event event) {
   	    Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
   	    optionalStripeObject.ifPresent(stripeObject -> {
   	     
   	Session session = (Session) stripeObject;

   	        

   	 
   	if (session.getPaymentIntent() != null) {
   	            
   	            
   	            
   	SessionRetrieveParams params = SessionRetrieveParams.builder().addExpand("payment_intent").build();

   	            try {
   	                session = Session.retrieve(session.getId(), params, null);
   	                Map<String, String> paymentIntentObject = session.getPaymentIntentObject().getMetadata();
   	                memberService.updateRole(paymentIntentObject);
   	            } 
   	catch (StripeException e) {
   	                e.printStackTrace();
   	            }
   	        } 
   	               
   	else {
   	            // Payment intent is null, directly use metadata from the session
   	            Map<String, String> metadata = session.getMetadata();
   	            memberService.updateRole(metadata);
   	        }
   	    });
   	}
}*/
