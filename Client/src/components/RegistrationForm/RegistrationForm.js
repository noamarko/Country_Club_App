import axios from 'axios';
import './RegistrationForm.css';
import React, { useState } from 'react';
import {API_BASE_URL} from '../../constants/apiContants';
import { withRouter } from "react-router-dom";


function RegistrationForm(props) {
  const [values, setValues] = useState({
    email:"",
    role:"",
    username:"",
    avatar:""
  });
  const [submitted, setSubmitted] = useState(false);
  const [valid, setValid] = useState(false);
  const [registered, setRegistered] = useState(false);

  const handleEmailInputChange = (event) => {
    setValues({...values, email: event.target.value})
  }
  const handleRoleInputChange = (event) => {
    setValues({...values, role: event.target.value})
  }
  const handleUsernameInputChange = (event) => {
    setValues({...values, username: event.target.value})
  }
  const handleAvatarInputChange = (event) => {
    setValues({...values, avatar: event.target.value})
  }
  const handleSubmit = (event) => {
      event.preventDefault();
      if(values.email && values.role && values.username && values.avatar){
        setValid(true);
        sendDetailsToServer();
        props.newUser(values);         //sendDetailsToServer already redirects to login
        props.updateTitle('Login');    //so I dont think it even gets here.
        props.history.push('/login'); 
      }
      setSubmitted(true);
  }  
  const redirectToHome = (event) => {
    event.preventDefault();
    props.updateTitle('Home');
    props.history.push('/home');   
  }

  const sendDetailsToServer = () => {
    if(values.email.length && values.role.length && values.username.length && values.avatar.length) {
        props.showError(null);
        const userDetails={
            "email":values.email.toLowerCase(),
            "role":values.role.toUpperCase(),
            "username":values.username,
            "avatar":values.avatar
        }
        axios.post(API_BASE_URL+'users', userDetails)
            .then(function (response) {
                if(response.status === 200){
                    setValues(prevState => ({
                        ...prevState,
                        'successMessage' : 'Registration successful. Redirecting to login page..'
                    }))
                    props.showError(null)
                } else{
                    props.showError("Some error ocurred12");
                }
            })
            .catch(function (error) {
                console.log(error);
            });    
    } else {
        props.showError('Please enter valid user details')    
    }
}


  return (
    <div className="form-container">
      <form className="register-form">
        {submitted && valid ? <div className="success-message">Success! Thank you for registering</div> : null}
        <div className="form-group text-left">
        <input
            onChange={handleEmailInputChange}
            value = {values.email}
            className="form-field"
            placeholder="Email"
            name="email" />
        </div>
        {submitted && !values.email ? <span>Please enter a valid email</span> : null}
        <div className="form-group text-left">
        <input
            onChange={handleRoleInputChange}
            value = {values.role}
            className="form-field"
            placeholder="Role"
            name="role" />
        </div>
        {submitted && !values.role ? <span>Please enter a valid role</span> : null}
        <div className="form-group text-left">
        <input
            onChange={handleUsernameInputChange}
            value = {values.username}
            className="form-field"
            placeholder="Username"
            name="space" />
        </div>
        {submitted && !values.username ? <span>Please enter a valid username</span> : null}
        <div className="form-group text-left">
        <input
            onChange={handleAvatarInputChange}
            value = {values.avatar}
            className="form-field"
            placeholder="Avatar"
            name="avatar" />
        </div>
        {submitted && !values.avatar ? <span>Please enter a valid avatar</span> : null}
        <button
            className="form-field"
            type="submit"
            onClick={handleSubmit}>Register</button>
         <button
            className="form-field"
            type="submit"
            onClick={redirectToHome}>Home</button>
      </form>
    </div>
  );
}

 export default withRouter(RegistrationForm);
