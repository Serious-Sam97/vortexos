import React from 'react';
import bgImage from '/clouds.jpg';
import w95 from '/w95.png';
import { MenuList, MenuListItem, Separator, styleReset, Button } from 'react95';
import styled from 'styled-components';
import { Link } from 'react-router';

const Intro: React.FC = () => {
    const ContentWrapper = styled.div`
        text-align: center;
    `;
    const BackgroundDiv = styled.div`
        width: 80vw;
        height: 80vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background-image: url(${bgImage});
        background-size: cover;
        background-position: center;
        border: 4px solid #000;
        box-shadow: 5px 5px 10px rgba(0, 0, 0, 0.5);
        padding: 20px;
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        `
    ;
    return (
        <BackgroundDiv>
            <ContentWrapper>
            <div>
                <img src={w95} width={200} className="logo" />
            </div>
            <h1>GameCache</h1>
            <div className="card">
                <Link to="/windows"><Button primary>Are you ready?</Button></Link>
            </div>
            <p className="read-the-docs">
                By Serious Sam
            </p>
            </ContentWrapper>
        </BackgroundDiv>
    );
};

export default Intro;