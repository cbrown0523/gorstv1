package com.careerdevs.gorstv1.controller;

import com.careerdevs.gorstv1.models.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

//@Rest controller is a combination of @controller & @ResponseBody--> eliminates the need to annotate every request handling method
@RestController
@RequestMapping("/api/user")
public class UserController {


    @Autowired
    Environment env;


    // simple get request - API key is not needed
    @GetMapping("/{id}")
    public Object getUser(
            @PathVariable("id") String userId,
            RestTemplate restTemplate){

        try {
            String url = "https://gorest.co.in/public/v2/users/" + userId;
            return restTemplate.getForObject(url, Object.class);

        }catch (Exception e){
            return "no user found ";
        }
    }

    @PostMapping("/")
    public ResponseEntity postUser(

            RestTemplate restTemplate,
            @RequestBody UserModel newUser
    ) {

        try {
            String url = "https://gorest.co.in/public/v2/users/";
            String token = env.getProperty("GOREST_TOKEN");
            url += "?access-token=" + token;


            HttpEntity<UserModel>request = new HttpEntity<>(newUser);

            return restTemplate.postForEntity(url, request, UserModel.class);

        }catch(Exception e){
            System.out.println(e.getClass() + "\n" + e.getMessage());

            //can't return a string b/c it is expecting a response entity.
//            //wii contain a status code along with e.getmessage a

            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    //PUT → use RequestBody to receive user data, use PathVariable to receive user ID

    @PutMapping("/{id}")
    public ResponseEntity putUser(@PathVariable("id") int updateUser,
                                  @RequestBody UserModel update,
                                  RestTemplate restTemplate){

        try{
            String url = "https://gorest.co.in/public/v2/users/" + updateUser;
            String token = env.getProperty("GOREST_TOKEN");
            url += "?access-token=" +token;

            HttpEntity<UserModel> request = new HttpEntity<>(update);
            ResponseEntity<UserModel> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    UserModel.class
            );

            return new ResponseEntity(response.getBody(), HttpStatus.OK );


        }catch (HttpClientErrorException.UnprocessableEntity e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            System.out.println(e.getClass() + "\n" + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }


    //DELETE → use PathVariaiable to receive user ID
    @DeleteMapping("/{id}")
    public Object deleteUser(@PathVariable("id") int deleteUserId,
                             RestTemplate restTemplate){
        try {
            String url = "https://gorest.co.in/public/v2/users/" + deleteUserId;
            String token = env.getProperty("GOREST_TOKEN");
            url += "?access-token=" + token;

//            HttpEntity<UserModel> deleteUser = new HttpEntity<>(deleteUserId);
            restTemplate.delete(url);
            return "user " + deleteUserId + " has been successfully deleted";
        }catch(HttpClientErrorException.NotFound exception){
            return "Error: user " + deleteUserId + " was not found";
        }catch (HttpClientErrorException.Unauthorized exception){
            return "you are not authorized to delete user: " + deleteUserId;

        } catch (Exception exception){
            System.out.println(exception.getClass());
            return exception.getMessage();


        }
    }


    //****************************************************************************
    // get all user on a page
    //http://localhost:4050/api/user/firstPage
    @GetMapping("/firstPage")
    //Rest template is needed to request external API.
    public Object getOnePage (RestTemplate restTemplate){

        try{
            String url = "https://gorest.co.in/public/v2/users/";
            // make a regular request and then save it.
            ResponseEntity<UserModel[]> firstPage = restTemplate.getForEntity(url, UserModel[].class);
            //get for entity will get the data from all the pages and then pick the headers apart.
            //FIRST PAGE HAS ALOT OF methods along with it
            //--> store first page in firstPage.getBody();
            //--> we store it in a user model[] array class.

            //if you only want the body you can store it in Object
            UserModel[] firstPageUsers =  firstPage.getBody();
            for(int  i = 0 ; i< firstPageUsers.length; i++){
                UserModel tempUser = firstPageUsers[i];
                System.out.println(tempUser.generatReport());
            }

//             return ResponseEntity<>(firstPage,HttpStatus.OK);
            return firstPage;


        }catch (Exception e){
            System.out.println(e.getClass());
            System.out.println(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }
    //-------working with headers -------
    @GetMapping("/firstp")
    //Rest template is needed to request external API.
    public Object getOneP (RestTemplate restTemplate){

        try{
            String url = "https://gorest.co.in/public/v2/users/";
            // make a regular request and then save it.
            ResponseEntity<UserModel[]> firstPageResponse = restTemplate.getForEntity(url, UserModel[].class);
            //get for entity will get the data from all the pages and then pick the headers apart.
            //FIRST PAGE HAS ALOT OF methods along with it
            //--> store first page in firstPage.getBody();
            //--> we store it in a user model[] array class.

            //if you only want the body you can store it in Object
            UserModel[] firstPageUsers =  firstPageResponse.getBody();

            HttpHeaders responseHeader = firstPageResponse.getHeaders();

            //READ headers
            String totalPages = Objects.requireNonNull(responseHeader.get("X-Pagination-Pages")).get(0);
            // -- error handling requirednonnull -- will stop program if its empty



            System.out.println("total pages: "+ totalPages);

            return new ResponseEntity<>(firstPageUsers ,HttpStatus.OK);



        }catch (Exception e){
            System.out.println(e.getClass());
            System.out.println(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }
    //-----------------------------Get all data using "X-Pagination-Pages" to get the total num of pages ----------------
    @GetMapping("/GetALL")
    public ResponseEntity getAll(RestTemplate restTemplate){
        try{
            // instantiate a "all users" arrayList.
            ArrayList<UserModel> allUsers = new ArrayList<>();
            // declare url thats going to be used
            String url =  "https://gorest.co.in/public/v2/users";

            ResponseEntity<UserModel[]> response = restTemplate.getForEntity(url, UserModel[].class);

            allUsers.addAll(Arrays.asList(Objects.requireNonNull(response.getBody())));

            HttpHeaders responseHeader = response.getHeaders();
            String totalPages = Objects.requireNonNull(responseHeader.get("X-Pagination-Pages")).get(0);
            int allPages = Integer.valueOf(totalPages);
            for (int i =0; i<= allPages; i++){
                String tempUrl = url + "?=page=" +i;
                UserModel[] pageData = restTemplate.getForObject(tempUrl, UserModel[].class);
                allUsers.addAll(Arrays.asList(Objects.requireNonNull(pageData)));
            }

            return new ResponseEntity(allUsers, HttpStatus.OK);

        }catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println(e.getClass());
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}