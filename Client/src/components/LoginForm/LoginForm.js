
import axios from 'axios';
import React, { useState } from 'react';
import { API_BASE_URL } from '../../constants/apiContants';
import country_club_outside from '../../country_club_outside.jpg';
import country_club_facebook from '../../country_club_facebook.png';
import country_club_instagram from '../../country_club_instagram.jpg';
import country_club_gym from '../../country_club_gym.jpg';
import { withRouter } from "react-router-dom";


function LoginForm(props) {

  const [values, setValues] = useState({
    space: "",
    email: ""
  });
  const [userDetails, setUserDetails] = useState({
    userId: { space: "", email: "" },
    role: "",
    username: "",
    avatar: ""
  });
  const [loggedIn, setLoggedIn] = useState(false);
  const [registered, setRegistered] = useState(false);
  const [valid, setValid] = useState(false);

  const redirectToRegister = (event) => {
    event.preventDefault();
    setRegistered(true);
    props.updateTitle('Register');
    props.history.push('/register');
  }
  const handleEmailInputChange = (event) => {
    setValues({ ...values, email: event.target.value })
  }
  const handleSpaceInputChange = (event) => {
    setValues({ ...values, space: event.target.value })
  }
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (values.email && values.space) {

      const tempUserDetails = await getDetailsFromServer();
      setUserDetails(tempUserDetails);
      console.log(userDetails.email + " " + userDetails.space);
      if (tempUserDetails.userId.email === values.email && tempUserDetails.userId.space === '2021b.Daniel.Aizenband') {
        setValid(true);
        props.loginDetails(tempUserDetails);
        props.updateTitle('Home');
        props.history.push('/home');
      }


    }
    setLoggedIn(true);
  }
  const handleLogout = (event) => {

  }

  const redirectToHome = (event) => {
    event.preventDefault();
    props.updateTitle('Home');
    props.history.push('/home');
  }

  const getDetailsFromServer = async () => {
    if (values.email.length && values.space.length) {
      props.showError(null);
      return await axios.get(API_BASE_URL + 'users/login/2021b.Daniel.Aizenband/' + values.email) //returns the requested object(according to the url) or none
        .then(function (response) {
          console.log(response.code + " " + response.data.code)
          if (response.status === 200) {
            setUserDetails(prevState => ({
              ...prevState,
              'successMessage': 'Login successful. Redirecting to home page..'
            }))
            props.showError(null)
            return response.data;
          }
          else if (response.data.code === 204) {
            props.showError("Space and email do not match");
          } else {
            props.showError("Some error ocurred");

          }
        })
        .catch(function (error) {
          console.log("Some error occured");
        });
    } else {
      props.showError('Please enter valid user details')
    }
  }

  return (
    <div className="Login-background">
      <header className="App-header">
        <label style={{ fontSize: '50px', color: 'black', fontWeight: 'bold' }}>{"Welcome to Country_Club_Name"}</label>
        <form className="login-form">
          {loggedIn && valid ? <div className="success-message">Success! Thank you for logging in</div> : null}
          <input
            onChange={handleSpaceInputChange}
            value={values.username}
            className="form-field"
            placeholder="Space"
            name="space" />
          {loggedIn && !values.space ? <span>Please enter a valid Space</span> : null}
          <input
            onChange={handleEmailInputChange}
            value={values.email}
            className="form-field"
            placeholder="Email"
            name="email" />
          {loggedIn && !values.email ? <label>Please enter a valid email</label> : null}
          <button
            className="form-field"
            type="submit"
            onClick={handleSubmit}>Log In</button>

        </form>
        <form className="registration-form">
          {registered ? <div className="registration-request">


          </div> : null}
          <button
            className="form-field"
            type="submit"
            onClick={redirectToRegister}>Register</button>



        </form>
        <img src={country_club_outside} className="App-country_club_outside" alt="country_club_outside" width="700" height="400" />
        <p style={{ fontSize: '30px', color: 'black', fontWeight: 'bold' }}>
          Find us on social medias!
        </p>
        <img src={country_club_facebook} className="App-country_club_facebook" alt="country_club_facebook" width="100" height="50" />
        <img src={country_club_instagram} className="App-country_club_instagram" alt="country_club_instagram" width="100" height="50" />
        <a
          className="App-link"
          href="https://www.ccbham.org/Club/Scripts/Home/home.asp"
          target="_blank"
          rel="noopener noreferrer"
        >

          Visit our website
        </a>
      </header>

    </div>
  )
}
export default withRouter(LoginForm);