import React, { useState } from 'react';
import axios from 'axios';
import { API_BASE_URL } from '../../constants/apiContants';
import { withRouter } from "react-router-dom";
import country_club_management from '../../country_club_management.jpg';
import country_club_member from '../../country_club_member.jpg';

function Home(props) {
  const [reserveSpotBar, setReserveSpotBar] = useState('default')
  const [reserveCourtBar, setReserveCourtBar] = useState('default')
  const [createItemBar, setCreateItemBar] = useState('default')
  const [createdMsg, setCreatedMsg] = useState(false)
  const [reservedSpotSuccess, setReservedSpotSuccess] = useState(false);
  const [reservedCourtSuccess, setReservedCourtSuccess] = useState(false);
  const [removedSpotSuccess, setRemovedSpotSuccess] = useState(false);
  const [removedCourtSuccess, setRemovedCourtSuccess] = useState(false);

  const redirectToLogin = (event) => {
    event.preventDefault();
    props.updateTitle('Login');
    props.history.push('/login');
  }
  const handleReserveSpotBar = (event) => {
    setReserveCourtBar('default');
    setCreateItemBar('default');
    setReserveSpotBar(event.target.value.toLowerCase());
  }

  const handleReserveCourtBar = (event) => {
    setReserveSpotBar('default');
    setCreateItemBar('default');
    setReserveCourtBar(event.target.value.toLowerCase());
  }
  const handleCreateItemBar = (event) => {
    setReserveSpotBar('default');
    setReserveCourtBar('default');
    setCreateItemBar(event.target.value.toLowerCase());
  }
  const [amount, setAmount] = useState(1);
  const handleAmountInputChange = (event) => {
    setAmount(event.target.value)
  }
  /*
  created operation pulled all items from database successfully
  located the right item but cant init operation.item.
  */
  const handleReserveSpot = async (event) => {
    var operationType = "";
    switch (reserveSpotBar) {
      case 'soccer': {
        if (event.target.className === 'reserve')
          operationType = 'reserveField';
        else
          operationType = 'cancelReservation'
        break
      }
      case 'tennis': {
        if (event.target.className === 'reserve')
          operationType = 'reserveField';
        else
          operationType = 'cancelReservation'
        break;
      }
      case 'sauna': {
        if (event.target.className === 'reserve')
          operationType = 'addUser';
        else
          operationType = 'removeUser'
        break;
      }
      case 'pool': {
        if (event.target.className === 'reserve')
          operationType = 'addUser';
        else
          operationType = 'removeUser'
        break;
      }
      case 'gym': {
        if (event.target.className === 'reserve')
          operationType = 'addUser';
        else
          operationType = 'removeUser'
        break;
      }
    }
    var operation = {
      operationId: { space: "", id: "" },
      type: operationType,
      item: {
        itemId: {
          space: "",
          id: ""
        }
      },
      createdTimestamp: "",
      invokedBy: {
        userId: {
          space: "",
          email: "",
        }
      },
      operationAttributes: {
        "playersAmount": amount
      }
    }
    const tempItemBoundaryArr = await getDetailsFromServer()
    if (tempItemBoundaryArr) {
      tempItemBoundaryArr.forEach(element => {
        if (element.name === reserveSpotBar) {
          operation.item.itemId = element.itemID;
          operation.invokedBy.userId = props.loginDetails.userId;
          sendOperationDetailsToServer(operation);
          setReservedCourtSuccess(false)
          setRemovedCourtSuccess(false)
          if (operationType == 'reserveField' || operationType == 'addUser'){
            setReservedSpotSuccess(true)
            setRemovedSpotSuccess(false)
          }
          else{
            setRemovedSpotSuccess(true)
            setReservedSpotSuccess(false)
          }
        }
      });
    }
  }

  const handleReserveCourt = async (event) => {
    var operationType = "";
    switch (reserveCourtBar) {
      case 'soccer': {
        if (event.target.className === 'reserve')
          operationType = 'reserveCourt';
        else
          operationType = 'cancelCourtReservation'
        break
      }
      case 'tennis': {
        if (event.target.className === 'reserve')
          operationType = 'reserveCourt';
        else
          operationType = 'cancelCourtReservation'
        break;
      }
    }
    var operation = {
      operationId: { space: "", id: "" },
      type: operationType,
      item: {
        itemId: {
          space: "",
          id: ""
        }
      },
      createdTimestamp: "",
      invokedBy: {
        userId: {
          space: "",
          email: "",
        }
      },
      operationAttributes: {
        "playersAmount": 1
      }
    }
    const tempItemBoundaryArr = await getDetailsFromServer()
    if (tempItemBoundaryArr) {
      tempItemBoundaryArr.forEach(element => {
        if (element.name === reserveCourtBar) {
          operation.item.itemId = element.itemID;
          operation.invokedBy.userId = props.loginDetails.userId;
          sendOperationDetailsToServer(operation);
          setRemovedSpotSuccess(false)
          setReservedSpotSuccess(false)
          if (operationType == 'reserveCourt'){
            setReservedCourtSuccess(true)
            setRemovedCourtSuccess(false)
          }
          else{
            setRemovedCourtSuccess(true)
            setReservedCourtSuccess(false)
          }
        }
      }
      );
    }

  }

  const handleCreateItem = () => {
    var itemType = "";
    var maxUsersAmount = "";
    switch (createItemBar) {
      case 'soccer': {
        itemType = 'sportsField';
        maxUsersAmount = 10;
        break;
      }
      case 'tennis': {
        itemType = 'sportsField';
        maxUsersAmount = 4;
        break;
      }
      case 'sauna': {
        itemType = 'sauna';
        maxUsersAmount = 6;
        break;
      }
      case 'pool': {
        itemType = 'pool';
        maxUsersAmount = 20;
        break;
      }
      case 'gym': {
        itemType = 'gym';
        maxUsersAmount = 10;
        break;
      }
      case 'towel': {
        itemType = 'object';
        maxUsersAmount = 1;
      }
    }
    const item = {
      itemId: { space: "", id: "" },
      type: itemType,
      name: createItemBar,
      active: true,
      createdTimestamp: "",
      createdBy: {
        userId: {
          space: "",
          email: "",
        }
      },
      location: {
        lat: "32.05147",
        lng: "34.76175"
      },
      itemAttributes: {
        "Max Users Amount": maxUsersAmount,
        "Current Users Amount": 0,
        "Reserved Fully": false
      }

    }
    sendItemDetailsToServer(item);
    setCreatedMsg(true);

  }

  const sendItemDetailsToServer = (item) => {
    if (item) {
      props.showError(null);
      axios.post(API_BASE_URL + 'items/' + props.loginDetails.userId.space + '/'
        + props.loginDetails.userId.email, item)
        .then(function (response) {
          if (response.data.code === 200) {

            props.showError(null)
          } else {
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
  const sendOperationDetailsToServer = (operation) => {
    if (operation) {
      props.showError(null);
      axios.post(API_BASE_URL + 'operations/', operation)
        .then(function (response) {
          if (response.data.code === 200) {
            props.showError(null)
          } else {
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
  const getDetailsFromServer = async () => {


    return await axios.get(API_BASE_URL + 'items/2021b.Daniel.Aizenband/' + props.loginDetails.userId.email) //returns the requested object(according to the url) or none
      .then(function (response) {
        if (response.status === 200) {
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
        console.log("Some error ocurred");
      });
  }




  return (
    <div className="App" style={{}}>
      {props.loginDetails.userId.email ?
        <h3 style={{ textAlign: 'left', color: 'black' }}>Welcome {props.loginDetails.userId.email
          .substring(0, props.loginDetails.userId.email.lastIndexOf('@'))} !</h3> : null
      }
      {props.loginDetails.role ?
        <h3 style={{ textAlign: 'left', color: 'black' }}>Role: {props.loginDetails.role}</h3>
        : null
      }
      {props.loginDetails.role ==='MANAGER' ? <img src={country_club_management} className="App-country_club_managemen" alt="country_club_management" width="70" height="50" align="left" />
      : <img src={country_club_member} className="App-country_club_member" alt="country_club_member" width="60" height="50" align="left" />}
      
      <div className="welcome">
        <form>
          <label for="cars" style={{ textAlign: 'left', color: 'black' }}>Reserve/Remove a spot</label>
          <select className='reserveSpotBar' id='reserveSpotBar' value={reserveSpotBar}
            style={{ width: 'fit-content' }}
            onChange={handleReserveSpotBar}>
            <optgroup label='Actions'>
              <option value='default'>----</option>
              <option value='pool'>Pool</option>
              <option value='soccer'>Soccer</option>
              <option value='tennis'>Tennis</option>
              <option value='gym'>Gym</option>
              <option value='sauna'>Sauna</option>
            </optgroup>
          </select>
          {reserveSpotBar === 'soccer' || reserveSpotBar === 'tennis' ? <input
            onChange={handleAmountInputChange}

            className="form-field"
            placeholder="Amount of Players" />
            : null}
          <button onClick={handleReserveSpot} className='reserve' type='button' style={{ position: 'relative', left: '1px' }}>Reserve</button>
          <button onClick={handleReserveSpot} className='remove' type='button' style={{ position: 'relative', left: '1px' }}>Remove</button>
          <div>
            <label for="cars" style={{ color: 'black' }}>Reserve/Remove court </label>
            <select className='reserveBar' id='reserveBar' value={reserveCourtBar}
              style={{ width: 'fit-content' }}
              onChange={handleReserveCourtBar}>
              <optgroup label='Actions'>
                <option value='default'>----</option>
                <option value='soccer'>Soccer</option>
                <option value='tennis'>Tennis</option>
              </optgroup>
            </select>
            <button onClick={handleReserveCourt} className='reserve' type='button' style={{ position: 'relative', left: '1px' }}>Reserve</button>
            <button onClick={handleReserveCourt} className='remove' type='button' style={{ position: 'relative', left: '1px' }}>Remove</button>
          </div>
          <div>
            {props.loginDetails.role.toUpperCase() === 'MANAGER' ?
              <div>
                <label for="items" style={{ color: 'black' }}>Create an Item     </label>
                <select className='itemsBar' id='itemsBar' value={createItemBar}
                  style={{ width: 'fit-content' }}
                  onChange={handleCreateItemBar}>
                  <optgroup label='Items'>
                    <option value='default'>----</option>
                    <option value='soccer'>Soccer field</option>
                    <option value='tennis'>Tennis court</option>
                    <option value='gym'>Gym room</option>
                    <option value='sauna'>Sauna room</option>
                    <option value='pool'>Pool</option>
                    <option value='towel'>Towel</option>
                  </optgroup>
                </select>
                <button onClick={handleCreateItem} type='button' style={{ position: 'relative', left: '1px' }}>Create</button>
              </div> : null}
          </div>
          <div>
          <br></br>
          {reservedSpotSuccess ? <div className="success-message" style={{fontSize:'26px', color:'limegreen'}}>Success! Spot reserved successfully</div> : null}
          {reservedCourtSuccess ? <div className="success-message"style={{fontSize:'26px', color:'limegreen'}}>Success! Court reserved successfully</div> : null}
          {removedSpotSuccess ? <div className="success-message"style={{fontSize:'26px', color:'red'}}>Success! Spot reservation canceled successfully</div> : null}
          {removedCourtSuccess ? <div className="success-message"style={{fontSize:'26px', color:'red'}}>Success! Court reservation canceled successfully</div> : null}
          {createdMsg ? <div className="success-message" style={{ fontSize: '26px', color: 'limegreen' }}>Success! Item created successfully</div> : null}
          </div>
          <header className="App-header">
            <button className="form-field"
              type="submit"
              onClick={redirectToLogin}>Logout</button>
          </header>
        </form>
      </div>
    </div>
  );
}
export default withRouter(Home);

